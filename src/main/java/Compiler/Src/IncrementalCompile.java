package Compiler.Src;

import org.antlr.v4.runtime.*;

import Compiler.Src.ASM.InstSelector;
import Compiler.Src.ASM.Allocator.RegAllocator;
import Compiler.Src.ASM.Allocator.StackManager;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.ASM.Node.ASMRoot;
import Compiler.Src.AST.*;
import Compiler.Src.AST.Node.*;

import Compiler.Src.Grammer.*;
import Compiler.Src.IR.IRBuilder;
import Compiler.Src.IR.Node.IRNode;
import Compiler.Src.IR.Node.IRRoot;
import Compiler.Src.IR.Node.Def.IRGlobalDef;
import Compiler.Src.Increment.SymbolNet.WorldCollector;
import Compiler.Src.Increment.SymbolNet.WorldScope;
import Compiler.Src.OPT.IROptimize;
import Compiler.Src.Semantic.*;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.MxErrorListener;
import java.nio.file.*;
import java.util.stream.Stream;
import java.io.*;
import java.util.*;
import java.security.MessageDigest; // 导入哈希相关类
import java.security.NoSuchAlgorithmException; // 导入哈希异常类

public class IncrementalCompile {
    private static final String CACHE_FILE = "build/cache.properties";
    // 源目录
    private static final String SRC_DIR = "src/test/in";
    // 生成的 asm 缓存目录
    private static final String OUT_DIR = "src/test/cache";
    // 最终合并输出
    private static final String FINAL_OUT = "test.s";
    // builtin.s
    private static final String BUILTIN = "builtin.s";

    public static void main(String[] args) throws IOException {
        Properties cache = new Properties();
        // 尝试加载缓存文件
        if (Files.exists(Paths.get(CACHE_FILE))) {
            try (InputStream in = Files.newInputStream(Paths.get(CACHE_FILE))) {
                cache.load(in);
            }
        }
        WorldScope worldScope = new WorldScope();
        WorldCollector worldCollector = new WorldCollector();
        HashMap<String, ASTNode> file2ast = new HashMap<>();
        String builtinFilePath = BUILTIN;

        // 读取 builtin.s 文件的内容
        String builtinContent = "";
        try {
            builtinContent = new String(Files.readAllBytes(Paths.get(builtinFilePath)));
        } catch (IOException e) {
            System.err.println("无法读取 builtin.s: " + e.getMessage());
            return;
        }

        // 收集所有 .mx 文件
        List<String> mxFiles = new ArrayList<>();
        try (Stream<Path> st = Files.walk(Paths.get(SRC_DIR))) {
            st.filter(p -> p.toString().endsWith(".mx"))
                    .forEach(p -> mxFiles.add(p.toString()));
        }
        Boolean incremental = true; // 增量编译标志，可用于强制完全重建
        List<String> toRecompile = new ArrayList<>(); // 需要重新编译的文件列表
        List<String> toSkip = new ArrayList<>(); // 可以跳过编译的文件列表

        // --- 哈希值判断文件修改的核心逻辑 ---
        for (String mx : mxFiles) {
            String currentHash = "";
            try {
                currentHash = calculateFileHash(mx); // 计算当前文件的哈希值
            } catch (NoSuchAlgorithmException e) {
                System.err.println("哈希算法未找到: " + e.getMessage());
                return;
            }

            String oldHash = cache.getProperty(mx); // 从缓存中获取旧的哈希值
            
            // 比较当前哈希值和旧哈希值，或者检查是否强制完全重建
            if (oldHash == null || !currentHash.equals(oldHash) || !incremental) {
                toRecompile.add(mx);
                // 立即更新缓存中已重新编译文件的哈希值
                cache.setProperty(mx, currentHash); 
            } else {
                toSkip.add(mx);
            }
        }
        // --- 哈希值判断文件修改的核心逻辑结束 ---
        
        String path = "build/worldscope";

        // 尝试从文件恢复 WorldScope
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(path)))) {
            worldScope = (WorldScope) ois.readObject();
            System.out.println("已从文件恢复 WorldScope,包含 funcs="
                    + worldScope.getFuncs().size()
                    + " classes=" + worldScope.getClasses().size()
                    + " Vars=" + worldScope.getVars().size()
                    + " Gvars=" + worldScope.getGvars().size());
        } catch (IOException | ClassNotFoundException e) {
            // 这可能发生在首次运行，或者文件损坏时。
            // 这里选择打印错误信息，并强制进行完全重新编译。
            System.err.println("无法从文件恢复 WorldScope。这可能是首次运行或文件已损坏。");
            e.printStackTrace();
            // 如果 WorldScope 无法加载，强制完全重新编译所有文件。
            toRecompile.clear();
            toSkip.clear();
            toRecompile.addAll(mxFiles);
            // 同时清空缓存，以确保完全重新计算哈希值
            cache.clear(); 
            System.out.println("由于 WorldScope 加载失败，强制进行完全重新编译。");
        }

        HashSet<String> addRecompileSet = new HashSet<>();
        // 处理文件之间的依赖关系，如果一个文件修改了，所有依赖它的文件也需要重新编译
        for(String filePath : toRecompile) {
            if(worldScope.getFile2related().get(filePath) == null)
            {
                worldScope.getFile2related().put(filePath, new HashSet<>());
                continue;
            }
            for(String related:worldScope.getFile2related().get(filePath))
            {
                if(!toRecompile.contains(related))
                {
                    addRecompileSet.add(related);
                }
            }
        }
        toRecompile.addAll(addRecompileSet);
        toSkip.removeAll(addRecompileSet); // 确保被添加到重新编译列表的文件不再跳过列表中

        worldScope.filter(toRecompile, toSkip);

        // 编译需要重新编译的文件
        for (String filePath : toRecompile) {
            System.out.println("正在编译: " + filePath);
            try {
                // 进行词法、语法分析和AST构建
                CharStream input = CharStreams.fromPath(Paths.get(filePath));
                MxLexer lexer = new MxLexer(input);
                lexer.removeErrorListeners();
                lexer.addErrorListener(new MxErrorListener());
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MxParser parser = new MxParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(new MxErrorListener());
                ASTNode astProgram = new ASTBuilder().visit(parser.program());
                file2ast.put(filePath, astProgram);
                worldCollector.scan((ASTRoot) astProgram, worldScope, filePath);
            } catch (BaseError e) {
                System.err.println("worldCollector 错误 in " + filePath + ": " + e.getMessage());
                return;
            }
        }

        // 处理全局变量
        try {
            ASTRoot globalVarProgram = worldCollector.GlobalVarCollectRelease(worldScope);
            worldCollector.inherit((ASTRoot) globalVarProgram, worldScope, true);
            new SemanticChecker().visit((ASTRoot) globalVarProgram);
            try {
                IRRoot irProgram = ((IRRoot) new IRBuilder("").visit((ASTRoot) globalVarProgram));
                for (HashSet<IRGlobalDef> defSet : worldScope.getFile2gcls().values()) {
                    for (IRGlobalDef def : defSet) {
                        irProgram.addDef(def);
                    }
                }
                new IROptimize().visit((IRRoot) irProgram);
                ASMNode asmProgram = new InstSelector().visit((IRRoot) irProgram);
                new RegAllocator((ASMRoot) asmProgram).Main();
                new StackManager().visit((ASMRoot) asmProgram);
                String outputIrFile = OUT_DIR + "/globalvar.s";
                Files.createDirectories(Paths.get(outputIrFile).getParent()); // 确保输出目录存在
                var outputAsm = new PrintStream(new FileOutputStream(outputIrFile));
                outputAsm.println(asmProgram);
                outputAsm.close();
            } catch (BaseError e) {
                System.err.println("全局变量IR/ASM生成错误: " +
                        e.getMessage());
                return;
            }
        } catch (BaseError e) {
            System.err.println("全局语义错误 : " +
                    e.getMessage());
            return;
        }

        // 继续处理需要重新编译的文件（语义检查、IR生成、ASM生成等）
        for (String filePath : toRecompile) {
            try {
                ASTNode astProgram = file2ast.get(filePath);
                System.out.println("进一步处理: " + filePath);
                if (astProgram != null) {
                    worldCollector.inherit((ASTRoot) astProgram, worldScope, false);
                    new SemanticChecker().visit((ASTRoot) astProgram);
                    worldCollector.consistRela((ASTRoot)astProgram,worldScope,filePath);
                    try {
                        String fileName = Paths.get(filePath).getFileName().toString(); 
                        IRNode irProgram = new IRBuilder(fileName).visit((ASTRoot) astProgram);
                        ((IRRoot) irProgram).Dinit();
                        new IROptimize().visit((IRRoot) irProgram);
                        ASMNode asmProgram = new InstSelector().visit((IRRoot) irProgram);
                        new RegAllocator((ASMRoot) asmProgram).Main();
                        new StackManager().visit((ASMRoot) asmProgram);
                        String outputIrFile = OUT_DIR + "/output_" +
                                fileName.replaceAll("\\.mx$", ".s");
                        Files.createDirectories(Paths.get(outputIrFile).getParent()); // 确保输出目录存在
                        var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                        outputIr.println(asmProgram);
                        outputIr.close();
                    } catch (BaseError e) {
                        System.err.println("IR/ASM生成错误 " + filePath + ": " +
                                e.getMessage());
                        return;
                    }
                }
            } catch (BaseError e) {
                System.err.println("语义错误 " + filePath + ": " +
                        e.getMessage());
                return;
            }
        }
        
        // 保存更新后的缓存（包含新的哈希值）
        Files.createDirectories(Paths.get(CACHE_FILE).getParent());
        try (OutputStream out = Files.newOutputStream(Paths.get(CACHE_FILE))) {
            cache.store(out, "incremental cache");
            System.out.println("缓存已更新，包含新的文件哈希值。");
        }

        // 合并所有生成的 .s 文件和 builtin.s
        try (PrintStream ps = new PrintStream(Files.newOutputStream(Paths.get(FINAL_OUT)))) {
            // 按文件名排序，保证输出顺序稳定
            Files.list(Paths.get(OUT_DIR))
                    .filter(p -> p.toString().endsWith(".s"))
                    .sorted((a, b) -> {
                        String na = a.getFileName().toString();
                        String nb = b.getFileName().toString();
                        if (na.equals("output_main.s"))
                            return -1; // 确保 main.s 在最前面（如果需要）
                        if (nb.equals("output_main.s"))
                            return 1;
                        return na.compareTo(nb);
                    })
                    .forEach(p -> {
                        try {
                            Files.lines(p).forEach(ps::println);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            // 将 builtin.s 的内容附加到最后
            Files.lines(Paths.get(BUILTIN)).forEach(ps::println);
        }

        System.out.println(">>> 完成，最终合并的汇编文件: " + FINAL_OUT);
        // 序列化 WorldScope 到文件，以便下次增量编译时恢复
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(path)))) {
            oos.writeObject(worldScope);
            System.out.println("WorldScope 已序列化到 " + path);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 计算文件的 SHA-256 哈希值。
     *
     * @param filePath 文件路径。
     * @return 文件的 SHA-256 哈希值（十六进制字符串）。
     * @throws IOException 如果读取文件时发生 I/O 错误。
     * @throws NoSuchAlgorithmException 如果 SHA-256 算法不可用。
     */
    private static String calculateFileHash(String filePath) throws IOException, NoSuchAlgorithmException {
        // 使用 SHA-256 算法，它具有良好的安全性，碰撞概率极低
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); 
        try (InputStream fis = Files.newInputStream(Paths.get(filePath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            // 逐块读取文件内容并更新哈希值
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashedBytes = digest.digest(); // 计算最终的哈希值
        return bytesToHex(hashedBytes); // 将字节数组转换为十六进制字符串
    }

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 要转换的字节数组。
     * @return 十六进制字符串表示。
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}