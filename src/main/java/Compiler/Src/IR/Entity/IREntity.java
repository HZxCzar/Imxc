package Compiler.Src.IR.Entity;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Setter
@lombok.Getter
public abstract class IREntity implements Externalizable {
    protected IRType type;
    protected String value;

    public IREntity() {
        this.type = null;
        this.value = null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1. 写出 type 是否存在
        out.writeBoolean(type != null);
        if (type != null) {
            out.writeObject(type);
        }
        // 2. 写出 value 是否存在
        out.writeBoolean(value != null);
        if (value != null) {
            out.writeUTF(value);
        }
    }

    /**
     * 反序列化：必须严格与 writeExternal 顺序对应
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1. 读回 type
        boolean hasType = in.readBoolean();
        if (hasType) {
            this.type = (IRType) in.readObject();
        } else {
            System.out.println("IREntity: type is null" +
                    " in readExternal, value: " + getValue());
            this.type = null;
        }
        // 2. 读回 value
        boolean hasValue = in.readBoolean();
        if (hasValue) {
            this.value = in.readUTF();
        } else {
            System.out.println("IREntity: value is null" +
                    " in readExternal, type: " + getType());
            this.value = null;
        }
    }

    public IREntity(IRType type, String value) {
        if (type.equals(GlobalScope.irBoolType)) {
            if (value.equals("true")) {
                value = "1";
            } else if (value.equals("false")) {
                value = "0";
            }
        }
        this.type = type;
        this.value = value;
    }

    public abstract String toString();

    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public boolean equals(Object obj) {
        if (obj instanceof IREntity) {
            return ((IREntity) obj).getValue().equals(getValue());
        }
        return false;
    }
}
