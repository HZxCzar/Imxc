package Compiler.Src.AST.Node.ExprNode.ExprUnitNode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTFstring extends ASTExpr {
    private ArrayList<String> strpart;
    private ArrayList<ASTExpr> exprpart;

    public ASTFstring() {
        super();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        // 1) 写 strpart
        if (strpart != null) {
            out.writeInt(strpart.size());
            for (String s : strpart) {
                out.writeUTF(s);
            }
        } else {
            out.writeInt(0);
        }

        // 2) 写 exprpart
        if (exprpart != null) {
            out.writeInt(exprpart.size());
            for (ASTExpr e : exprpart) {
                out.writeObject(e);
            }
        } else {
            out.writeInt(0);
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        // 1) 读 strpart
        int sSize = in.readInt();
        strpart = new ArrayList<>(sSize);
        for (int i = 0; i < sSize; i++) {
            strpart.add(in.readUTF());
        }

        // 2) 读 exprpart
        int eSize = in.readInt();
        exprpart = new ArrayList<>(eSize);
        for (int i = 0; i < eSize; i++) {
            @SuppressWarnings("unchecked")
            ASTExpr e = (ASTExpr) in.readObject();
            exprpart.add(e);
        }
    }
}