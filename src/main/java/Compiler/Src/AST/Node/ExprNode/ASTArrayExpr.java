package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTArrayExpr extends ASTExpr {
    private final ASTExpr arrayName,index;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
