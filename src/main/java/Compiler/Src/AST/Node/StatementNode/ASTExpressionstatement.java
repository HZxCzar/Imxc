package Compiler.Src.AST.Node.StatementNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTExpressionstatement extends ASTStatement {
    private final ArrayList<ASTExpr> expr;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
