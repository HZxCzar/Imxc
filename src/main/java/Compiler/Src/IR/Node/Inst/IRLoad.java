package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRLoad extends IRInst {
    private IRType type;
    private IRVariable dest,ptr;
    // private IREntity ptr;

    public IRLoad(int id,IRVariable dest, IRVariable ptr) {
        super(id);
        this.type = dest.getType();
        this.dest = dest;
        this.ptr = ptr;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return dest.getValue() + " = load " + type.toString() + ", " + ptr.toString();
    }

    @Override
    public IRVariable getDest() {
        return dest;
    }

    @Override
    public IRVariable getDef() {
        return dest;
    }

    @Override
    public ArrayList<IRVariable> getUses() {
        ArrayList<IRVariable> res = new ArrayList<>();
        if (ptr instanceof IRVariable) {
            res.add((IRVariable) ptr);
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        if (ptr.equals(oldVar)) {
            ptr = (IRVariable)newVar;
        }
    }
}
