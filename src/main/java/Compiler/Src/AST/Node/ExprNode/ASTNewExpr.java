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
    private final TypeInfo type;
    private final ArrayList<ASTExpr> size;
    private final ASTConstarray constarray;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
