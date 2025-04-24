package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
// import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Info.ExprInfo;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter

public class ASTAtomExpr extends ASTExpr {
    public static enum Type {
        INT, BOOL, STRING, FSTRING, CONSTARRAY, INDENTIFIER, NULL, THIS;
    }

    // private ExprInfo secondInfo;
    private Type atomType;
    private String value;
    private ASTConstarray constarray;
    private ASTFstring fstring;
    public ASTAtomExpr() {
        super();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 如果父类有可序列化字段，先写父类
        super.writeExternal(out);

        // 1) 枚举：写 ordinal（也可以直接 writeObject(atomType)）
        out.writeInt(atomType.ordinal());

        // 2) 简单字段
        out.writeUTF(value == null ? "" : value);
        // 或者如果需要区分 null / ""，可改成：
        // out.writeBoolean(value != null);
        // if (value != null) out.writeUTF(value);

        // 3) 复合字段，直接 writeObject（支持 null）
        out.writeObject(constarray);
        out.writeObject(fstring);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 如果父类有可序列化字段，先读父类
        super.readExternal(in);

        // 1) 枚举
        int ord = in.readInt();
        this.atomType = Type.values()[ord];

        // 2) 简单字段
        this.value = in.readUTF();
        // 如果上面写入时用了 boolean+writeUTF：
        // if (in.readBoolean()) this.value = in.readUTF();
        // else this.value = null;

        // 3) 复合字段
        @SuppressWarnings("unchecked")
        ASTConstarray ca = (ASTConstarray) in.readObject();
        this.constarray = ca;

        @SuppressWarnings("unchecked")
        ASTFstring fs = (ASTFstring) in.readObject();
        this.fstring = fs;
    }
}
