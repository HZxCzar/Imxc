package Compiler.Src.OPT;

import Compiler.Src.IR.Node.IRRoot;

public class IROptimize {
    public void visit(IRRoot root)
    {
        new LoadStoreOpt().visit(root);
        new Mem2Reg().visit(root);
        new IROther().visit(root);
        new LoopOpt().visit(root);
        new Tail().visit(root);
        new Inlining().visit(root);
        new ADCE().visit(root);
        new SCCP().visit(root);
        new CSE().visit(root);
        new RovB().visit(root);
        new LiveAnalysis().visit(root);
        return;
    }
}
