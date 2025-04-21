package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRRet extends IRInst {
    private boolean voidtype;
    private IRType type;
    private IREntity value;

    public IRRet(int id,IREntity value) {
        super(id);
        this.voidtype = false;
        this.type = value.getType();
        this.value = value;
    }

    public IRRet(int id) {
        super(id);
        this.voidtype = true;
        this.type = GlobalScope.irVoidType;
        this.value = null;
    }

    @Override
    public IRVariable getDef() {
        return null;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        if(voidtype) return "ret void";
        return "ret " + type.toString() + " " + value.getValue();
    }

    @Override
    public ArrayList<IRVariable> getUses() {
        ArrayList<IRVariable> res = new ArrayList<>();
        if (value instanceof IRVariable) {
            res.add((IRVariable) value);
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        if (value.equals(oldVar)) {
            this.value = newVar;
        }
    }
}
