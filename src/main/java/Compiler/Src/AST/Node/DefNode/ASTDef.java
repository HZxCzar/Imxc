package Compiler.Src.AST.Node.DefNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.*;
import Compiler.Src.AST.Node.*;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;
import lombok.Getter;
import lombok.Setter;
@lombok.experimental.SuperBuilder
@Getter
@Setter
public class ASTDef extends ASTNode {
    private BaseInfo info;
    public ASTDef() {
        super();
        this.info = null;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    
    public String findName()
    {
        return getInfo().getName();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1) 先写父类那一部分（ASTDef/ASTNode 中的字段）
        super.writeExternal(out);

        // 2) 再写本类特有的 initexpr
        out.writeBoolean(info != null);
        if (info != null) {
            out.writeObject(info);
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1) 先读回父类那一部分
        super.readExternal(in);

        // 2) 再读本类特有的 initexpr
        boolean hasInfo = in.readBoolean();
        if (hasInfo) {
            this.info = (BaseInfo) in.readObject();
        } else {
            System.out.println("ASTDef: info is null"+
                    " in readExternal, name: " + getPos());
            this.info = null;
        }
    }
}
