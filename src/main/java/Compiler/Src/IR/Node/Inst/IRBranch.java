package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRBranch extends IRInst {
    private boolean isJump = false;
    private IREntity cond;
    private IRLabel trueLabel, falseLabel;

    public IRBranch(int id,IREntity cond, IRLabel trueLabel, IRLabel falseLabel) {
        super(id);
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
        this.isJump = false;
    }

    public IRBranch(int id,IRLabel jumpLabel) {
        super(id);
        this.cond = new IRLiteral(GlobalScope.irBoolType, "true");
        this.trueLabel = jumpLabel;
        this.falseLabel = jumpLabel;
        this.isJump = true;
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
        if(isJump) return "br label %" + trueLabel;
        return "br " + cond.toString() + ", label %" + trueLabel + ", label %" + falseLabel;
    }

    @Override
    public ArrayList<IRVariable> getUses() {
        ArrayList<IRVariable> res = new ArrayList<>();
        if (cond instanceof IRVariable) {
            res.add((IRVariable) cond);
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        if (cond.equals(oldVar)) {
            cond = newVar;
        }
    }
}
