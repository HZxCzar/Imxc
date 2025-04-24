package Compiler.Src.AST.Node.DefNode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.ExprInfo;
import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.Util.Info.VarInfo;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTVarDef extends ASTDef{
    private ASTExpr initexpr;
    public ASTVarDef() {
        super();
        this.initexpr = null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1) 先写父类那一部分（ASTDef/ASTNode 中的字段）
        super.writeExternal(out);

        // 2) 再写本类特有的 initexpr
        out.writeBoolean(initexpr != null);
        if (initexpr != null) {
            out.writeObject(initexpr);
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1) 先读回父类那一部分
        super.readExternal(in);

        // 2) 再读本类特有的 initexpr
        boolean hasInit = in.readBoolean();
        if (hasInit) {
            this.initexpr = (ASTExpr) in.readObject();
            // 恢复父子关系
            this.initexpr.setParent(this);
        } else {
            this.initexpr = null;
        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    public TypeInfo getVarType() {
        return ((VarInfo) getInfo()).getType();
    }
}
