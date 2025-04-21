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
import Compiler.Src.OPT.IROptimize;
import Compiler.Src.Semantic.*;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.MxErrorListener;

public class Compile_base {
    public static void main(String[] args) throws IOException {
        try {
            CharStream input = CharStreams.fromStream(new FileInputStream("src/test/mx/input.mx"));
            // new FileInputStream("src/test/mx/input.mx")
            MxLexer lexer = new MxLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MxParser parser = new MxParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ASTNode astProgram = new ASTBuilder().visit(parser.program());
            new SymbolCollector().visit((ASTRoot) astProgram);
            new SemanticChecker().visit((ASTRoot) astProgram);
            try {
                IRNode irProgram = new IRBuilder().visit((ASTRoot) astProgram);
                new IROptimize().visit((IRRoot) irProgram);
                var output2 = new PrintStream(new FileOutputStream("src/test/mx/output.ll"));
                output2.println(irProgram);
                output2.close();
                // System.out.println(irProgram);

                
                // ASMNode asmProgram2 = new ASMBuilder_Formal().visit((IRRoot) irProgram);
                ASMNode asmProgram2 = new InstSelector().visit((IRRoot) irProgram);
                new RegAllocator((ASMRoot)asmProgram2).Main();
                new StackManager().visit((ASMRoot)asmProgram2);
                var codegenOutput2 = new PrintStream(new FileOutputStream("src/test/mx/test.s"));
                codegenOutput2.println(asmProgram2);
                codegenOutput2.close();

                String filePath = "builtin.s"; // 文件路径

                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
                System.out.println(asmProgram2);
            } catch (BaseError e) {
                System.out.println(e.getMessage());
                // System.exit(1);
                return;
            }
        } catch (BaseError e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        // System.out.println("Compile Successfully");
    }
}
