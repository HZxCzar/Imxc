package Compiler.Src.AST.Node.ExprNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTArrayExpr extends ASTExpr {
    private ASTExpr arrayName, index;

    public ASTArrayExpr() {
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
        out.writeObject(arrayName);
        // 写 index 节点
        out.writeObject(index);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        @SuppressWarnings("unchecked")
        ASTExpr arr = (ASTExpr) in.readObject();
        this.arrayName = arr;

        @SuppressWarnings("unchecked")
        ASTExpr idx = (ASTExpr) in.readObject();
        this.index = idx;
    }
}
