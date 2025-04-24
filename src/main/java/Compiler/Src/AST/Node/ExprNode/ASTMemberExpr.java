package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTMemberExpr extends ASTExpr {
    private ASTExpr member;
    private String memberName;

    public ASTMemberExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        // 1) 写 member 节点
        out.writeObject(member);
        // 2) 写 memberName
        out.writeUTF(memberName);
    }
    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 member
        @SuppressWarnings("unchecked")
        ASTExpr m = (ASTExpr) in.readObject();
        this.member = m;
        // 2) 读 memberName
        this.memberName = in.readUTF();
    }
}
