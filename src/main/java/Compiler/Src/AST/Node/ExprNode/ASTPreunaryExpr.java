package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTPreunaryExpr extends ASTExpr {
    private ASTExpr expr;
    private String op;

    public ASTPreunaryExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        // 1) 写 expr 节点
        out.writeObject(expr);
        // 2) 写 op
        out.writeUTF(op);
    }
    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 expr
        @SuppressWarnings("unchecked")
        ASTExpr e = (ASTExpr) in.readObject();
        this.expr = e;
        // 2) 读 op
        this.op = in.readUTF();
    }
}
