package Compiler.Src.IR.Node.Def;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRStructType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRGlobalDef extends IRDef implements Externalizable {
    private IRVariable vars;

    public IRGlobalDef() {
        super();
    }

    public IRGlobalDef(IRVariable vars) {
        this.vars = vars;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 写出 vars 是否存在
        out.writeBoolean(vars != null);
        if (vars != null) {
            out.writeObject(vars);
        }
    }

    /**
     * 反序列化：必须严格与 writeExternal 顺序对应
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 读回 vars
        boolean hasVars = in.readBoolean();
        if (hasVars) {
            this.vars = (IRVariable) in.readObject();
        } else {
            System.out.println("IRGlobalDef: vars is null" +
                    " in readExternal");
            this.vars = null;
        }
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String str = vars.getValue() + " = global ";
        if (vars.getType().equals(GlobalScope.irIntType) || vars.getType().equals(GlobalScope.irBoolType)) {
            str += new IRLiteral(vars.getType(), "0").toString();
        } else if (vars.getType() instanceof IRStructType) {
            str = vars.getValue() + " = type " + vars.getType().toString();
        } else {
            str += "ptr null";
        }
        return str;
    }
}
