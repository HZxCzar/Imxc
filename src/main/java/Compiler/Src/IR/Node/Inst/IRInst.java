package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;
import java.util.Objects;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.IRNode;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.IRError;
import Compiler.Src.Util.Error.OPTError;

@lombok.Getter
@lombok.Setter
public class IRInst extends IRNode implements Comparable<IRInst> {
    protected int id;
    public IRInst(int id) {
        this.id = id;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public IRVariable getDest() {
        throw new IRError("IRInst.getDest() is not implemented");
    }

    public IRVariable getDef() {
        throw new IRError("IRInst.getDest() is not implemented");
    }

    public ArrayList<IRVariable> getUses() {
        throw new IRError("IRInst.getUses() is not implemented");
    }

    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        throw new IRError("IRInst.replaceUse() is not implemented");
    }

    @Override
    public int compareTo(IRInst o) {
        if(this instanceof IRLabel && o instanceof IRLabel){
            return ((IRLabel) this).getLabel().compareTo(((IRLabel) o).getLabel());
        }
        return this.id - o.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
