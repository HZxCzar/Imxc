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

public class IncrementalCompile {
    private static final Path CACHE_FILE = Paths.get("build/cache.properties");
    // 源目录
    private static final Path SRC_DIR = Paths.get("src/test/in");
    // 生成的 asm 缓存目录
    private static final Path OUT_DIR = Paths.get("src/test/cache");
    // 最终合并输出
    private static final Path FINAL_OUT = Paths.get("test.s");
    // builtin.s
    private static final Path BUILTIN = Paths.get("builtin.s");

    public static void main(String[] args) throws IOException {
        Properties cache = new Properties();
        if (Files.exists(CACHE_FILE)) {
            try (InputStream in = Files.newInputStream(CACHE_FILE)) {
                cache.load(in);
            }
        }
        // Set the directory path containing the input files
        WorldScope worldScope = new WorldScope();
        WorldCollector worldCollector = new WorldCollector();
        HashMap<String, ASTNode> file2ast = new HashMap<>();
        String builtinFilePath = "builtin.s";

        // Read the contents of builtin.s
        String builtinContent = "";
        try {
            builtinContent = new String(Files.readAllBytes(Paths.get(builtinFilePath)));
        } catch (IOException e) {
            System.err.println("Failed to read builtin.s: " + e.getMessage());
            return;
        }

        // Collect all .mx files from the directory
        List<Path> mxFiles = new ArrayList<>();
        try (Stream<Path> st = Files.walk(SRC_DIR)) {
            st.filter(p -> p.toString().endsWith(".mx"))
                    .forEach(mxFiles::add);
        }
        Boolean incremental = true;
        List<Path> toRecompile = new ArrayList<>();
        List<Path> toSkip = new ArrayList<>();
        for (Path mx : mxFiles) {
            long now = Files.getLastModifiedTime(mx).toMillis();
            long old = Long.parseLong(cache.getProperty(mx.toString(), "0"));
            if (now != old | !incremental) {
                toRecompile.add(mx);
                cache.setProperty(mx.toString(), String.valueOf(now));
            } else {
                toSkip.add(mx);
            }
        }

        String path = "build/worldscope.txt";

        // 3. 从文件恢复
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(path)))) {
            worldScope = (WorldScope) ois.readObject();
            // 反序列化时会先 new WorldScope()（无参构造器），
            // 再调用 loaded.readExternal(...)
            System.out.println("已从文件恢复 WorldScope,包含 funcs="
                    + worldScope.getFuncs().size()
                    + " classes=" + worldScope.getClasses().size()
                    + " Vars=" + worldScope.getVars().size()
                    + " Gvars=" + worldScope.getGvars().size());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        worldScope.filter(toRecompile, toSkip);

        for (Path filePath : toRecompile) {
            System.out.println("Compiling: " + filePath);
            try {
                // Process each file as before
                CharStream input = CharStreams.fromPath(filePath);
                MxLexer lexer = new MxLexer(input);
                lexer.removeErrorListeners();
                lexer.addErrorListener(new MxErrorListener());
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MxParser parser = new MxParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(new MxErrorListener());
                ASTNode astProgram = new ASTBuilder().visit(parser.program());
                file2ast.put(filePath.toString(), astProgram);
                worldCollector.scan((ASTRoot) astProgram, worldScope, filePath.toString());
            } catch (BaseError e) {
                System.err.println("worldCollector error in " + filePath + ": " + e.getMessage());
            }
        }

        // process globalvar
        try {
            ASTRoot globalVarProgram = worldCollector.GlobalVarCollectRelease(worldScope);
            worldCollector.inherit((ASTRoot) globalVarProgram, worldScope, true);
            new SemanticChecker().visit((ASTRoot) globalVarProgram);
            try {
                IRNode irProgram = new IRBuilder().visit((ASTRoot) globalVarProgram);
                new IROptimize().visit((IRRoot) irProgram);
                ASMNode asmProgram = new InstSelector().visit((IRRoot) irProgram);
                new RegAllocator((ASMRoot) asmProgram).Main();
                new StackManager().visit((ASMRoot) asmProgram);
                String outputIrFile = "src/test/cache/globalvar.s";
                var outputAsm = new PrintStream(new FileOutputStream(outputIrFile));
                outputAsm.println(asmProgram);
                outputAsm.close();
                // CombinedAsm.combine((ASMRoot) asmProgram, true);

            } catch (BaseError e) {
                System.err.println("Error during IR/ASM generation for GlobalVar: " +
                        e.getMessage());
            }
        } catch (BaseError e) {
            System.err.println("Semantic error in global : " +
                    e.getMessage());
        }

        for (Path filePath : toRecompile) {
            try {
                ASTNode astProgram = file2ast.get(filePath.toString());
                // process .mx files
                System.out.println("Further processing: " + filePath);
                if (astProgram != null) {
                    worldCollector.inherit((ASTRoot) astProgram, worldScope, false);
                    new SemanticChecker().visit((ASTRoot) astProgram);

                    try {
                        IRNode irProgram = new IRBuilder().visit((ASTRoot) astProgram);
                        ((IRRoot) irProgram).Dinit();
                        new IROptimize().visit((IRRoot) irProgram);
                        ASMNode asmProgram = new InstSelector().visit((IRRoot) irProgram);
                        new RegAllocator((ASMRoot) asmProgram).Main();
                        new StackManager().visit((ASMRoot) asmProgram);
                        // ((ASMRoot) asmProgram).Dinit();
                        String outputIrFile = "src/test/cache/output_" +
                                filePath.getFileName().toString().replaceAll("\\.mx$", ".s");
                        var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                        outputIr.println(asmProgram);
                        outputIr.close();
                        // CombinedAsm.combine((ASMRoot) asmProgram, false);

                    } catch (BaseError e) {
                        System.err.println("Error during IR/ASM generation for " + filePath + ": " +
                                e.getMessage());
                    }
                }
            } catch (BaseError e) {
                System.err.println("Semantic error in " + filePath + ": " +
                        e.getMessage());
            }
        }
        Files.createDirectories(CACHE_FILE.getParent());
        try (OutputStream out = Files.newOutputStream(CACHE_FILE)) {
            cache.store(out, "incremental cache");
        }

        try (PrintStream ps = new PrintStream(Files.newOutputStream(FINAL_OUT))) {
            // // 如果需要 builtin
            // if (Files.exists(BUILTIN)) {
            // Files.lines(BUILTIN).forEach(ps::println);
            // }
            // 按文件名排序，保证顺序稳定
            Files.list(OUT_DIR)
                    .filter(p -> p.toString().endsWith(".s"))
                    .sorted((a, b) -> {
                        String na = a.getFileName().toString();
                        String nb = b.getFileName().toString();
                        if (na.equals("output_main.s"))
                            return -1; // a 是 main 放前
                        if (nb.equals("output_main.s"))
                            return 1; // b 是 main 放前
                        return na.compareTo(nb);
                    })
                    .forEach(p -> {
                        try {
                            Files.lines(p).forEach(ps::println);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        System.out.println(">>> done, final merged asm: " + FINAL_OUT);
        // 2. 持久化到文件
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(path)))) {
            oos.writeObject(worldScope); // 会调用 world.writeExternal(...)
            System.out.println("WorldScope 已序列化到 " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
