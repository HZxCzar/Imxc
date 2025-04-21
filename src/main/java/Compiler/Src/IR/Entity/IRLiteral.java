package Compiler.Src.IR.Entity;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRLiteral extends IREntity {
    public IRLiteral(IRType type, String value) {
        super(type, (type.equals(GlobalScope.irPtrType) && value.equals("0")) ? "null" : value);
    }

    @Override
    public String toString() {
        return getType().toString() + " " + getValue();
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
