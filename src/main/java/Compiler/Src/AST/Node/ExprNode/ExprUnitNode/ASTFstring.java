package Compiler.Src.AST.Node.ExprNode.ExprUnitNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTFstring extends ASTExpr {
    private ArrayList<String> strpart;
    private ArrayList<ASTExpr> exprpart;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
