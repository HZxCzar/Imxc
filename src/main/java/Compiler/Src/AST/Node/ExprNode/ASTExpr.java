package Compiler.Src.AST.Node.ExprNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.*;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter

public class ASTExpr extends ASTNode {
    private ExprInfo Info;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
