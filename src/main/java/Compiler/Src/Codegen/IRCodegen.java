package Compiler.Src.Codegen;

import Compiler.Src.IR.Node.IRRoot;

public class IRCodegen {
    public void visit(IRRoot node) {
        System.out.println(node);
    }
}
