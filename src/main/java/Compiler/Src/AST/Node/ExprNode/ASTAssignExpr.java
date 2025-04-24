package Compiler.Src.AST.Node.ExprNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTAssignExpr extends ASTExpr {
    private ASTExpr left, right;

    public ASTAssignExpr() {
        super();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        // 写 arrayName 节点
        out.writeObject(left);
        // 写 index 节点
        out.writeObject(right);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        @SuppressWarnings("unchecked")
        ASTExpr left = (ASTExpr) in.readObject();
        this.left = left;

        @SuppressWarnings("unchecked")
        ASTExpr right = (ASTExpr) in.readObject();
        this.right = right;
    }
}
