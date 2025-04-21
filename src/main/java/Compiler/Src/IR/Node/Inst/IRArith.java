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
// import Compiler.Src.Util.ScopeUtil.GlobalScope;
import Compiler.Src.Util.Error.IRError;

@lombok.Getter
@lombok.Setter
public class IRArith extends IRInst {
    private String op;
    private IRType type;
    private IREntity lhs, rhs;
    private IRVariable dest;

    public IRArith(int id, IRVariable dest, String op, IRType type, IREntity lhs, IREntity rhs) {
        super(id);
        this.dest = dest;
        this.type = type;
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return dest.getValue() + " = " + op + " " + lhs.getType().toString() + " " + lhs.getValue() + ", "
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

    public IREntity Innercompute(HashMap<IRVariable, Pair<Integer, IREntity>> varMap) {
        if (lhs instanceof IRVariable && varMap.get((IRVariable) lhs).a == 0) {
            return null;
        } else if (rhs instanceof IRVariable && varMap.get((IRVariable) rhs).a == 0) {
            return null;
        }
        int lval = lhs instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) lhs).b.getValue())
                : Integer.parseInt(lhs.getValue());
        int rval = rhs instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) rhs).b.getValue())
                : Integer.parseInt(rhs.getValue());
        var res = new IRLiteral(dest.getType(), "0");
        switch (op) {
            case "add" -> {
                res.setValue(String.valueOf(lval + rval));
            }
            case "sub" -> {
                res.setValue(String.valueOf(lval - rval));
            }
            case "mul" -> {
                res.setValue(String.valueOf(lval * rval));
            }
            case "sdiv" -> {
                if (rval == 0) {
                    res.setValue("0");
                } else {
                    res.setValue(String.valueOf(lval / rval));
                }
            }
            case "srem" -> {
                if (rval == 0) {
                    res.setValue("0");
                } else {
                    res.setValue(String.valueOf(lval % rval));
                }
            }
            case "shl" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs <<
                // rhs));
                res.setValue(String.valueOf(lval << rval));
            }
            case "ashr" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs >>
                // rhs));
                res.setValue(String.valueOf(lval >> rval));
            }
            case "and" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs &
                // rhs));
                res.setValue(String.valueOf(lval & rval));
            }
            case "or" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs |
                // rhs));
                res.setValue(String.valueOf(lval | rval));
            }
            case "xor" -> {
                // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs ^
                // rhs));
                res.setValue(String.valueOf(lval ^ rval));
            }
            default -> throw new ASMError("Unknown Binary operation");
        }
        return res;
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
