package Compiler.Src;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Compile {
    public static void main(String[] args) throws IOException {
        // Set the directory path containing the input files
        WorldScope worldScope = new WorldScope();
        String inputDir = "src/test/in";
        WorldCollector worldCollector = new WorldCollector();
        StringBuilder combinedAsmBuilder = new StringBuilder();
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
        List<Path> filesToCompile;
        try {
            try (Stream<Path> paths = Files.walk(Paths.get(inputDir))) {
                filesToCompile = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".mx"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("Failed to read input directory: " + e.getMessage());
            return;
        }

        for (Path filePath : filesToCompile) {
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
                System.err.println("Semantic error in " + filePath + ": " + e.getMessage());
            }
        }
        // Process IR
        ASMRoot CombinedAsm = new ASMRoot();

        String mainFilePath = null;
        String mainFile = null;

        // process main.mx
        for (Path filePath : filesToCompile) {
            if (filePath.getFileName().toString().equals("main.mx")) {
                mainFilePath = filePath.toString();
                mainFile = filePath.getFileName().toString();
                break;
            }
        }
        if (mainFilePath != null) {
            try {
                ASTNode astProgram = file2ast.get(mainFilePath);
                // process main.mx
                System.out.println("Further processing: " + mainFilePath);
                worldCollector.inherit((ASTRoot) astProgram, worldScope, true);
                worldCollector.GlobalVarCollectRelease((ASTRoot) astProgram, worldScope);
                new SemanticChecker().visit((ASTRoot) astProgram);

                try {
                    IRNode irProgram = new IRBuilder().visit((ASTRoot) astProgram);
                    new IROptimize().visit((IRRoot) irProgram);
                    ASMNode asmProgram = new InstSelector().visit((IRRoot)irProgram);
                    new RegAllocator((ASMRoot) asmProgram).Main();
                    new StackManager().visit((ASMRoot) asmProgram);
                    String outputIrFile = "src/test/cache/output_" +
                            mainFile.replaceAll("\\.mx$", ".s");
                    var outputAsm = new PrintStream(new FileOutputStream(outputIrFile));
                    outputAsm.println(asmProgram);
                    outputAsm.close();
                    CombinedAsm.combine((ASMRoot) asmProgram, true);

                } catch (BaseError e) {
                    System.err.println("Error during IR/ASM generation for " + mainFilePath + ": " +
                            e.getMessage());
                }
            } catch (BaseError e) {
                System.err.println("IR error in " + mainFilePath + ": " +
                        e.getMessage());
            }
        }

        for (Path filePath : filesToCompile) {
            try {
                ASTNode astProgram = file2ast.get(filePath.toString());
                // process other .mx files
                if (!filePath.getFileName().toString().equals("main.mx")) {
                    System.out.println("Further processing: " + filePath);
                    if (astProgram != null) {
                        worldCollector.inherit((ASTRoot) astProgram, worldScope, false);
                        new SemanticChecker().visit((ASTRoot) astProgram);

                        try {
                            IRNode irProgram = new IRBuilder().visit((ASTRoot) astProgram);
                            new IROptimize().visit((IRRoot) irProgram);
                            ASMNode asmProgram = new InstSelector().visit((IRRoot)irProgram);
                            new RegAllocator((ASMRoot) asmProgram).Main();
                            new StackManager().visit((ASMRoot) asmProgram);
                            String outputIrFile = "src/test/cache/output_" +
                                    filePath.getFileName().toString().replaceAll("\\.mx$", ".s");
                            var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                            outputIr.println(asmProgram);
                            outputIr.close();
                            CombinedAsm.combine((ASMRoot) asmProgram, false);

                        } catch (BaseError e) {
                            System.err.println("Error during IR/ASM generation for " + filePath + ": " +
                                    e.getMessage());
                        }
                    }
                }
            } catch (BaseError e) {
                System.err.println("IR error in " + filePath + ": " +
                        e.getMessage());
            }
        }
        
        // Write the combined assembly code to a single output file
        String singleOutputAsmFile = "test.s";
        try (PrintStream combinedOutputStream = new PrintStream(new FileOutputStream(singleOutputAsmFile))) {
            // combinedOutputStream.println(builtinContent);
            combinedOutputStream.println(CombinedAsm.toString());
        }

        System.out.println("Compilation finished. Combined output written to " + singleOutputAsmFile);
    }
}
