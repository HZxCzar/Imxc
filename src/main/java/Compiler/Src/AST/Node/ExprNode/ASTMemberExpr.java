package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTMemberExpr extends ASTExpr {
    private final ASTExpr member;
    private final String memberName;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
