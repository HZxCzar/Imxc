package Compiler.Src.AST.Node.ExprNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.ASTConstarray;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.ASTFstring;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTBinaryExpr extends ASTExpr {
    private ASTExpr left,right;
    private String op;

    public ASTBinaryExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 如果父类有 writeExternal，先调父类
        super.writeExternal(out);

        // 1) 写 left 子节点
        out.writeObject(left);

        // 2) 写 right 子节点
        out.writeObject(right);

        // 3) 写运算符。这里假设 op 不为 null，
        //    如果可能为 null，可以先写一个 boolean 标记
        out.writeUTF(op);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 如果父类有 readExternal，先调父类
        super.readExternal(in);

        // 1) 读 left
        @SuppressWarnings("unchecked")
        ASTExpr l = (ASTExpr) in.readObject();
        this.left = l;

        // 2) 读 right
        @SuppressWarnings("unchecked")
        ASTExpr r = (ASTExpr) in.readObject();
        this.right = r;

        // 3) 读 op
        this.op = in.readUTF();
    }
}

