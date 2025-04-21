package Compiler.Src.IR.Node;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.Util.Error.BaseError;

public class IRNode {
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    @Override
    public String toString() {
        return "IRNode should not be printed";
    }
}
