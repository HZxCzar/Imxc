package Compiler.Src.AST.Node.ExprNode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.*;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter

public class ASTExpr extends ASTNode{
    private ExprInfo Info;
    public ASTExpr() {
        super();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1) 序列化父类部分（pos 信息等）
        super.writeExternal(out);

        // 2) 序列化本类部分 Info
        out.writeBoolean(Info != null);
        if (Info != null) {
            out.writeObject(Info);
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1) 反序列化父类部分
        super.readExternal(in);

        // 2) 反序列化本类部分 Info
        boolean hasInfo = in.readBoolean();
        if (hasInfo) {
            this.Info = (ExprInfo) in.readObject();
        } else {
            this.Info = null;
        }
    }
}
