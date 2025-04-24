package Compiler.Src.Util.Info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@lombok.Getter
@lombok.Setter
public class BaseInfo implements Externalizable {
    private String name;

    public BaseInfo() {
        this.name = "";
    }

    public BaseInfo(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return false;
    }

    /**
     * 序列化：写出 name
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // UTF 不支持 null，若 name 可能为 null，可改成 out.writeBoolean(name!=null) + writeUTF
        out.writeUTF(name);
    }

    /**
     * 反序列化：读入 name
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
    }
}
