package Compiler.Src.IR.Node.Def;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;

@lombok.Value
@lombok.EqualsAndHashCode(callSuper = true)
public class IRClassDef extends IRDef {
    private ArrayList<IRType> vars;
    private ArrayList<IRFuncDef> funcs;

    public IRClassDef(ArrayList<IRType> vars, ArrayList<IRFuncDef> funcs) {
        this.vars = vars;
        this.funcs = funcs;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return null;
    }
}
