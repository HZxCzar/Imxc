package Compiler.Src.AST.Node.StatementNode;

import Compiler.Src.AST.Node.ASTNode;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.StatementNode.ASTStatement;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTStatement extends ASTNode {
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
