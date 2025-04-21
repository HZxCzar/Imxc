package Compiler.Src.IR.Entity;

import java.util.Objects;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRVariable extends IREntity implements Comparable<IRVariable> {
    public IRVariable(IRType type, String value) {
        super(type, value);
    }

    public boolean isGlobal() {
        return getValue().startsWith("@");
    }
    public boolean isStr()
    {
        return getValue().startsWith("@str");
    }

    public boolean isParameter() {
        return !getValue().startsWith("%.tmp.") && !getValue().startsWith("@");//!getValue().startsWith("%.tmp.") && 
    }

    @Override
    public String toString() {
        return getType().toString() + " " + getValue();
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public int compareTo(IRVariable rhs) {
        return getValue().compareTo(((IRVariable) rhs).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
