package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTConditionalExpr extends ASTExpr {
    private ASTExpr Ques,left,right;

    public ASTConditionalExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        // 1) 写 Ques 节点
        out.writeObject(Ques);

        // 2) 写 left 节点
        out.writeObject(left);

        // 3) 写 right 节点
        out.writeObject(right);
    }
    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 Ques
        @SuppressWarnings("unchecked")
        ASTExpr q = (ASTExpr) in.readObject();
        this.Ques = q;

        // 2) 读 left
        @SuppressWarnings("unchecked")
        ASTExpr l = (ASTExpr) in.readObject();
        this.left = l;

        // 3) 读 right
        @SuppressWarnings("unchecked")
        ASTExpr r = (ASTExpr) in.readObject();
        this.right = r;
    }
}
