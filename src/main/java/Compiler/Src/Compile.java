package Compiler.Src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import Compiler.Src.Codegen.IRCodegen;
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
        IRRoot CombinedIr = new IRRoot();

        String mainFilePath = null;
        String mainFile = null;

        // 先处理 main.mx
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
                    String outputIrFile = "src/test/mx/output_" +
                            mainFile.replaceAll("\\.mx$", ".ll");
                    var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                    outputIr.println(irProgram);
                    outputIr.close();
                    CombinedIr.combine((IRRoot) irProgram);

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
                            String outputIrFile = "src/test/mx/output_" +
                                    filePath.getFileName().toString().replaceAll("\\.mx$", ".ll");
                            var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
                            outputIr.println(irProgram);
                            outputIr.close();
                            CombinedIr.combine((IRRoot) irProgram);

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

        // IR optimization and ASM generation
        try {
            new IROptimize().visit(CombinedIr);
            String outputIrFile = "src/test/mx/output_opt.ll";
            var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
            outputIr.println(CombinedIr);
            outputIr.close();

            ASMNode asmProgram2 = new InstSelector().visit(CombinedIr);
            new RegAllocator((ASMRoot) asmProgram2).Main();
            new StackManager().visit((ASMRoot) asmProgram2);

            // Write individual .s file for incremental compilation
            String outputAsmFile = "src/test/mx/output.s";
            try (PrintStream fileOutputStream = new PrintStream(new FileOutputStream(outputAsmFile))) {
                fileOutputStream.println(asmProgram2);
            }

            // Append to the combined assembly
            combinedAsmBuilder.append(asmProgram2.toString()).append("\n");

        } catch (BaseError e) {
            System.err.println("Error during IR/ASM generation: " + e.getMessage());
        }
        // for (Path filePath : filesToCompile) {
        // System.out.println("Further processing: " + filePath);
        // try {
        // ASTNode astProgram = file2ast.get(filePath.toString());
        // if (astProgram != null) {
        // worldCollector.inherit((ASTRoot) astProgram, worldScope);
        // new SemanticChecker().visit((ASTRoot) astProgram);

        // try {
        // IRNode irProgram = new IRBuilder().visit((ASTRoot) astProgram);
        // String outputIrFile = "src/test/mx/output_" +
        // filePath.getFileName().toString().replaceAll("\\.mx$", ".ll");
        // var outputIr = new PrintStream(new FileOutputStream(outputIrFile));
        // outputIr.println(irProgram);
        // outputIr.close();
        // new IROptimize().visit((IRRoot) irProgram);
        // String outputIrOpFile = "src/test/mx/output_opt_" +
        // filePath.getFileName().toString().replaceAll("\\.mx$", ".ll");
        // var outputIrOp = new PrintStream(new FileOutputStream(outputIrOpFile));
        // outputIrOp.println(irProgram);
        // outputIrOp.close();

        // ASMNode asmProgram2 = new InstSelector().visit((IRRoot) irProgram);
        // new RegAllocator((ASMRoot) asmProgram2).Main();
        // new StackManager().visit((ASMRoot) asmProgram2);

        // // Write individual .s file for incremental compilation
        // String outputAsmFile = "src/test/mx/output_" +
        // filePath.getFileName().toString().replaceAll("\\.mx$", ".s");
        // try (PrintStream fileOutputStream = new PrintStream(new
        // FileOutputStream(outputAsmFile))) {
        // fileOutputStream.println(asmProgram2);
        // }

        // // Append to the combined assembly
        // combinedAsmBuilder.append(asmProgram2.toString()).append("\n");

        // } catch (BaseError e) {
        // System.err.println("Error during IR/ASM generation for " + filePath + ": " +
        // e.getMessage());
        // }
        // }
        // } catch (BaseError e) {
        // System.err.println("IR error in " + filePath + ": " +
        // e.getMessage());
        // }
        // }

        // Write the combined assembly code to a single output file
        String singleOutputAsmFile = "test.s";
        try (PrintStream combinedOutputStream = new PrintStream(new FileOutputStream(singleOutputAsmFile))) {
            // combinedOutputStream.println(builtinContent);
            combinedOutputStream.println(combinedAsmBuilder.toString());
        }

        System.out.println("Compilation finished. Combined output written to " + singleOutputAsmFile);
    }
}
