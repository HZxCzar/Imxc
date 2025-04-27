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
        if (Files.exists(Paths.get(CACHE_FILE))) {
            try (InputStream in = Files.newInputStream(Paths.get(CACHE_FILE))) {
                cache.load(in);
            }
        }
        WorldScope worldScope = new WorldScope();
        WorldCollector worldCollector = new WorldCollector();
        HashMap<String, ASTNode> file2ast = new HashMap<>();
        String builtinFilePath = BUILTIN;

        // Read the contents of builtin.s
        String builtinContent = "";
        try {
            builtinContent = new String(Files.readAllBytes(Paths.get(builtinFilePath)));
        } catch (IOException e) {
            System.err.println("Failed to read builtin.s: " + e.getMessage());
            return;
        }

        // Collect all .mx files from the directory
        List<String> mxFiles = new ArrayList<>();
        try (Stream<Path> st = Files.walk(Paths.get(SRC_DIR))) {
            st.filter(p -> p.toString().endsWith(".mx"))
                    .forEach(p -> mxFiles.add(p.toString()));
        }
        Boolean incremental = true;
        List<String> toRecompile = new ArrayList<>();
        List<String> toSkip = new ArrayList<>();

        for (String mx : mxFiles) {
            long now = Files.getLastModifiedTime(Paths.get(mx)).toMillis();
            long old = Long.parseLong(cache.getProperty(mx, "0"));
            if (now != old | !incremental) {
                toRecompile.add(mx);
                cache.setProperty(mx, String.valueOf(now));
            } else {
                toSkip.add(mx);
            }
        }
        

        String path = "build/worldscope";

        // 3. 从文件恢复
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
            e.printStackTrace();
        }

        HashSet<String> addRecompileSet = new HashSet<>();
        // //打印File2related
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        // for(String filePath : worldScope.getFile2related().keySet())
        // {
        //     System.out.println("File: " + filePath);
        //     for(String related:worldScope.getFile2related().get(filePath))
        //     {
        //         System.out.println("Related File: " + filePath + " to: " + related);
        //     }
        // }
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for(String filePath : toRecompile) {
            if(worldScope.getFile2related().get(filePath) == null)
            {
                worldScope.getFile2related().put(filePath, new HashSet<>());
                continue;
            }
            for(String related:worldScope.getFile2related().get(filePath))
            {
                // System.out.println("Related File: " + filePath + " to: " + related);
                if(!toRecompile.contains(related))
                {
                    addRecompileSet.add(related);
                }
            }
        }
        toRecompile.addAll(addRecompileSet);
        toSkip.removeAll(addRecompileSet);

        worldScope.filter(toRecompile, toSkip);

        for (String filePath : toRecompile) {
            System.out.println("Compiling: " + filePath);
            try {
                // Process each file as before
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
                System.err.println("worldCollector error in " + filePath + ": " + e.getMessage());
                return;
            }
        }

        // process globalvar
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
                var outputAsm = new PrintStream(new FileOutputStream(outputIrFile));
                outputAsm.println(asmProgram);
                outputAsm.close();
            } catch (BaseError e) {
                System.err.println("Error during IR/ASM generation for GlobalVar: " +
                        e.getMessage());
                return;
            }
        } catch (BaseError e) {
            System.err.println("Semantic error in global : " +
                    e.getMessage());
            return;
        }

        for (String filePath : toRecompile) {
            try {
                ASTNode astProgram = file2ast.get(filePath);
                // process .mx files
                System.out.println("Further processing: " + filePath);
                if (astProgram != null) {
                    worldCollector.inherit((ASTRoot) astProgram, worldScope, false);
                    new SemanticChecker().visit((ASTRoot) astProgram);
                    worldCollector.consistRela((ASTRoot)astProgram,worldScope,filePath);
                    try {
                        String fileName = Paths.get(filePath).getFileName().toString(); // 保持文件名正确
                        IRNode irProgram = new IRBuilder(fileName).visit((ASTRoot) astProgram);
                        ((IRRoot) irProgram).Dinit();
                        new IROptimize().visit((IRRoot) irProgram);
                        ASMNode asmProgram = new InstSelector().visit((IRRoot) irProgram);
                        new RegAllocator((ASMRoot) asmProgram).Main();
                        new StackManager().visit((ASMRoot) asmProgram);
                        String outputIrFile = OUT_DIR + "/output_" +
                                fileName.replaceAll("\\.mx$", ".s");
                        var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                        outputIr.println(asmProgram);
                        outputIr.close();
                    } catch (BaseError e) {
                        System.err.println("Error during IR/ASM generation for " + filePath + ": " +
                                e.getMessage());
                        return;
                    }
                }
            } catch (BaseError e) {
                System.err.println("Semantic error in " + filePath + ": " +
                        e.getMessage());
                return;
            }
        }
        //打印File2related
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        // for(String filePath : worldScope.getFile2related().keySet())
        // {
        //     System.out.println("File: " + filePath);
        //     for(String related:worldScope.getFile2related().get(filePath))
        //     {
        //         System.out.println("Related File: " + filePath + " to: " + related);
        //     }
        // }
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Files.createDirectories(Paths.get(CACHE_FILE).getParent());
        try (OutputStream out = Files.newOutputStream(Paths.get(CACHE_FILE))) {
            cache.store(out, "incremental cache");
        }

        try (PrintStream ps = new PrintStream(Files.newOutputStream(Paths.get(FINAL_OUT)))) {
            // 按文件名排序，保证顺序稳定
            Files.list(Paths.get(OUT_DIR))
                    .filter(p -> p.toString().endsWith(".s"))
                    .sorted((a, b) -> {
                        String na = a.getFileName().toString();
                        String nb = b.getFileName().toString();
                        if (na.equals("output_main.s"))
                            return -1;
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
        }

        System.out.println(">>> done, final merged asm: " + FINAL_OUT);
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
}