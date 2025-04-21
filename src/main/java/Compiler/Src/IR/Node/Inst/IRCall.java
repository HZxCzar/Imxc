package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.IRError;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRCall extends IRInst {
    private IRVariable dest;
    private IRType type;
    private String funcName;
    private ArrayList<IREntity> args;

    public IRCall(int id,String funcName, ArrayList<IREntity> args) {
        super(id);
        this.type = GlobalScope.irVoidType;
        this.dest = null;
        this.funcName = funcName;
        this.args = args;
        // for(var arg: args) {
        //     if (arg == null) {
        //         throw new OPTError("what");
        //     }
        // }
    }

    public IRCall(int id,IRVariable dest, IRType type, String funcName, ArrayList<IREntity> args) {
        super(id);
        this.dest = dest;
        this.type = type;
        this.funcName = funcName;
        this.args = args;
        // for(var arg: args) {
        //     if (arg == null) {
        //         throw new OPTError("what");
        //     }
        // }
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String str = (dest == null ? "" : dest.getValue() + " = ") + "call " + type.toString();
        str += " @" + funcName + "(";
        for (int i = 0; i < args.size(); i++) {
            str += args.get(i).getType().toString() + " " + args.get(i).getValue();
            if (i != args.size() - 1) {
                str += ", ";
            }
        }
        str += ")";
        return str;
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
        for(var arg: args) {
            if (arg instanceof IRVariable) {
                res.add((IRVariable) arg);
            }
        }
        return res;
    }

    @Override
    public void replaceUse(IRVariable oldVar, IREntity newVar) {
        for(int i = 0; i < args.size(); i++) {
            // if(args.get(i)==null) {
            //     throw new OPTError("null");
            // }
            if (args.get(i).equals(oldVar)) {
                args.set(i, newVar);
                return;
            }
        }
    }
}
