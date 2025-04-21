package Compiler.Src.IR.Node.Def;

import Compiler.Src.IR.Node.IRNode;
import Compiler.Src.IR.IRVisitor;
import Compiler.Src.Util.Error.BaseError;

public class IRDef extends IRNode {
    public IRDef() {
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
