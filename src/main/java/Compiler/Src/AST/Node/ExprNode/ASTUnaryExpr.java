package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTUnaryExpr extends ASTExpr {
    private final ASTExpr expr;
    private final String op;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
