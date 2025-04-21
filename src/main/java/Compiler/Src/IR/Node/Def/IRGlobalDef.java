package Compiler.Src.IR.Node.Def;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Type.IRStructType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRGlobalDef extends IRDef {
    private IRVariable vars;

    public IRGlobalDef(IRVariable vars) {
        this.vars = vars;
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
