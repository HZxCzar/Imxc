package Compiler.Src.Util.Info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@lombok.Getter
@lombok.Setter
public class ExprInfo extends BaseInfo{
    private BaseInfo type;
    private boolean isLvalue;
    public ExprInfo() {
        super("");
        this.type = null;
        this.isLvalue = false;
    }
    public ExprInfo(String name,BaseInfo type,boolean isLvalue)
    {
        super(name);
        this.type=type;
        this.isLvalue=isLvalue;
    }
    
    public TypeInfo getDepTypeInfo()
    {
        BaseInfo type=this.getType();
        if(type instanceof TypeInfo)
        {
            return (TypeInfo)type;
        }
        else if(type instanceof VarInfo)
        {
            return ((VarInfo)type).getType();
        }
        else{
            return null;
        }
    }

    /**
     * 按固定顺序把所有字段写入
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1. 写父类 name
        super.writeExternal(out);
        // 2. 写 type 是否为 null
        out.writeBoolean(type != null);
        if (type != null) {
            out.writeObject(type);
        }
        // 3. 写 isLvalue
        out.writeBoolean(isLvalue);
    }

    /**
     * 必须与 writeExternal 完全对应，按顺序读回字段
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1. 读 name 并设回父类
        super.readExternal(in);
        // 2. 读 type
        boolean hasType = in.readBoolean();
        if (hasType) {
            this.type = (BaseInfo) in.readObject();
        } else {
            this.type = null;
        }
        // 3. 读 isLvalue
        this.isLvalue = in.readBoolean();
    }
}
