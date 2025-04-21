package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IROptBranch extends IRInst {
    public boolean isEq = false;
    private IREntity cond1, cond2;
    private IRLabel trueLabel, falseLabel;

    public IROptBranch(int id,IREntity cond1,IREntity cond2, IRLabel trueLabel, IRLabel falseLabel, boolean isEq) {
        super(id);
        this.cond1 = cond1;
        this.cond2 = cond2;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
        this.isEq = isEq;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public IRVariable getDef() {
        return null;
    }

    @Override
    public String toString() {
        throw new OPTError("IROptBranch.toString() is not implemented");
    }

    @Override
    public ArrayList<IRVariable> getUses() {
        ArrayList<IRVariable> res = new ArrayList<>();
        if (cond1 instanceof IRVariable) {
            res.add((IRVariable) cond1);
        }
        if (cond2 instanceof IRVariable) {
            res.add((IRVariable) cond2);
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        if (cond1.equals(oldVar)) {
            cond1 = newVar;
        }
        else if (cond2.equals(oldVar)) {
            cond2 = newVar;
        }
    }
}
