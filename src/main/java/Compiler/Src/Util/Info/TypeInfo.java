package Compiler.Src.Util.Info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

// import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class TypeInfo extends BaseInfo {
    private int depth;
    private boolean defined;

    public TypeInfo(String name, int depth) {
        super(name);
        this.depth = depth;
        if (name.equals("int") || name.equals("void") || name.equals("bool") || name.equals("string")
                || name.equals("null")) {
            this.defined = true;
        } else {
            this.defined = false;
        }
    }

    public TypeInfo() {
        super("");
        this.depth = 0;
        this.defined = false;
    }

    /**
     * 按顺序把 name/depth/defined 写出
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1. 写 BaseInfo.name
        super.writeExternal(out);
        // 2. 写自己的字段
        out.writeInt(depth);
        out.writeBoolean(defined);
    }

    /**
     * 必须以与 writeExternal 完全对应的顺序读回
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1. 读 name 并赋回给父类
        super.readExternal(in);

        // 2. 读 depth
        this.depth = in.readInt();

        // 3. 读 defined
        this.defined = in.readBoolean();
    }

    @Override
    public boolean equals(Object rhs) {
        if (!(rhs instanceof TypeInfo)) {
            return false;
        }
        if (this.getName().equals(((TypeInfo) rhs).getName()) && this.depth == ((TypeInfo) rhs).depth) {
            return true;
        }
        return false;
    }
}
