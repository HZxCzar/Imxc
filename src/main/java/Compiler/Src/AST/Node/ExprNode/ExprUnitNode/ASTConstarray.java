package Compiler.Src.AST.Node.ExprNode.ExprUnitNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.ExprInfo;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTConstarray extends ASTExpr {
    private int maze, dep;
    private ArrayList<ASTExpr> expr;
    private IRVariable dest;

    public ASTConstarray() {
        super();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    /**
     * Write out all of the fields in a specific order.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        // primitive fields
        out.writeInt(maze);
        out.writeInt(dep);

        // write the list: first its size, then each element
        if (expr != null) {
            out.writeInt(expr.size());
            for (ASTExpr e : expr) {
                out.writeObject(e);
            }
        } else {
            out.writeInt(0);
        }

        // write the dest variable
        out.writeObject(dest);
    }

    /**
     * Read them back in the exact same order.
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        // primitives
        maze = in.readInt();
        dep = in.readInt();

        // read list
        int n = in.readInt();
        expr = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            ASTExpr e = (ASTExpr) in.readObject();
            expr.add(e);
        }

        // read dest
        dest = (IRVariable) in.readObject();
    }
}
