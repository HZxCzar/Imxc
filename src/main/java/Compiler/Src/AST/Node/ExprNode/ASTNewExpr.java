package Compiler.Src.AST.Node.ExprNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.ASTConstarray;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTNewExpr extends ASTExpr {
    private TypeInfo type;
    private ArrayList<ASTExpr> size;
    private ASTConstarray constarray;

    public ASTNewExpr() {
        super();
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        // 1) 写 type 节点
        out.writeObject(type);

        // 2) 写 size 节点
        if (size != null) {
            out.writeInt(size.size());
            for (ASTExpr s : size) {
                out.writeObject(s);
            }
        } else {
            out.writeInt(0);
        }

        // 3) 写 constarray 节点
        out.writeObject(constarray);
    }
    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 type
        @SuppressWarnings("unchecked")
        TypeInfo t = (TypeInfo) in.readObject();
        this.type = t;

        // 2) 读 size
        int sizeSize = in.readInt();
        size = new ArrayList<>(sizeSize);
        for (int i = 0; i < sizeSize; i++) {
            @SuppressWarnings("unchecked")
            ASTExpr s = (ASTExpr) in.readObject();
            size.add(s);
        }

        // 3) 读 constarray
        @SuppressWarnings("unchecked")
        ASTConstarray ca = (ASTConstarray) in.readObject();
        this.constarray = ca;
    }
}
