package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
// import Compiler.Src.Util.Info.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter

public class ASTAtomExpr extends ASTExpr {
    public static enum Type {
        INT, BOOL, STRING, FSTRING, CONSTARRAY, INDENTIFIER, NULL, THIS;
    }
    // private ExprInfo secondInfo;
    private Type atomType;
    private String value;
    private ASTConstarray constarray;
    private ASTFstring fstring;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
