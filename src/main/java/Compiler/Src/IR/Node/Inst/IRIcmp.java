package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.ASMError;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRIcmp extends IRInst {
    private String cond;
    private IRType type;
    private IREntity lhs, rhs;
    private IRVariable dest;

    public IRIcmp(int id, IRVariable dest, String cond, IRType type, IREntity lhs, IREntity rhs) {
        super(id);
        this.cond = cond;
        this.type = type;
        this.lhs = lhs;
        this.rhs = rhs;
        this.dest = dest;
    }

    public IREntity Innercompute(HashMap<IRVariable, Pair<Integer, IREntity>> varMap) {
        if (lhs instanceof IRVariable && varMap.get((IRVariable) lhs).a == 0) {
            return null;
        } else if (rhs instanceof IRVariable && varMap.get((IRVariable) rhs).a == 0) {
            return null;
        }
        if ((lhs instanceof IRVariable && varMap.get((IRVariable) lhs).b == null)
                || (rhs instanceof IRVariable && varMap.get((IRVariable) rhs).b == null)) {
            int a = 1;
        }
        int lval = lhs instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) lhs).b.getValue().equals("null")?"0":varMap.get((IRVariable) lhs).b.getValue())
                : Integer.parseInt(lhs.getValue().equals("null")?"0":lhs.getValue());
        int rval = rhs instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) rhs).b.getValue().equals("null")?"0":varMap.get((IRVariable) rhs).b.getValue())
                : Integer.parseInt(rhs.getValue().equals("null")?"0":rhs.getValue());
        var res = new IRLiteral(dest.getType(), "0");
        switch (cond) {
            case "eq" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs == rhs ? 1 : 0));
                res.setValue(lval == rval ? "1" : "0");
            }
            case "ne" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs != rhs ? 1 : 0));
                res.setValue(lval != rval ? "1" : "0");
            }
            case "slt" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs < rhs ? 1 : 0));
                res.setValue(lval < rval ? "1" : "0");
            }
            case "sgt" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs > rhs ? 1 : 0));
                res.setValue(lval > rval ? "1" : "0");
            }
            case "sle" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs <= rhs ? 1 : 0));
                res.setValue(lval <= rval ? "1" : "0");
            }
            case "sge" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                // lhs >= rhs ? 1 : 0));
                res.setValue(lval >= rval ? "1" : "0");
            }
            default -> throw new ASMError("Unknown Binary operation");
        }
        return res;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return dest.getValue() + " = icmp " + cond + " " + lhs.getType().toString() + " " + lhs.getValue() + ", "
                + rhs.getValue();
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
        if (lhs instanceof IRVariable) {
            res.add((IRVariable) lhs);
        }
        if (rhs instanceof IRVariable) {
            res.add((IRVariable) rhs);
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        if (lhs.equals(oldVar)) {
            lhs = newVar;
        }
        if (rhs.equals(oldVar)) {
            rhs = newVar;
        }
    }
}
