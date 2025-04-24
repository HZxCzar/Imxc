package Compiler.Src.Util.Info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@lombok.Getter
@lombok.Setter
public class VarInfo extends BaseInfo {
    TypeInfo type;

    public VarInfo() {
        super("");
        this.type = null;
    }

    public VarInfo(String name, TypeInfo type) {
        super(name);
        this.type = type;
    }

    /**
     * 序列化：按顺序把 name/type 写入
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1. 写出父类的 name
        super.writeExternal(out);
        // 2. 写出 type 是否存在
        out.writeBoolean(type != null);
        if (type != null) {
            out.writeObject(type);
        }
    }

    /**
     * 反序列化：必须严格与 writeExternal 顺序对应
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1. 读回 name 并赋值给父类
        super.readExternal(in);
        // 2. 读回 type
        boolean hasType = in.readBoolean();
        if (hasType) {
            this.type = (TypeInfo) in.readObject();
        } else {
            System.out.println("VarInfo: type is null"+
                    " in readExternal, name: " + getName());
            this.type = null;
        }
    }

}
