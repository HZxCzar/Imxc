package Compiler.Src.AST.Node.ExprNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTCallExpr extends ASTExpr {
    private ASTExpr func;
    private ArrayList<ASTExpr> args;

    public ASTCallExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        // 1) 写 func 节点
        out.writeObject(func);

        // 2) 写 args 节点
        if (args != null) {
            out.writeInt(args.size());
            for (ASTExpr arg : args) {
                out.writeObject(arg);
            }
        } else {
            out.writeInt(0);
        }
    }
    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 func
        @SuppressWarnings("unchecked")
        ASTExpr f = (ASTExpr) in.readObject();
        this.func = f;

        // 2) 读 args
        int argSize = in.readInt();
        args = new ArrayList<>(argSize);
        for (int i = 0; i < argSize; i++) {
            @SuppressWarnings("unchecked")
            ASTExpr arg = (ASTExpr) in.readObject();
            args.add(arg);
        }
    }
}
