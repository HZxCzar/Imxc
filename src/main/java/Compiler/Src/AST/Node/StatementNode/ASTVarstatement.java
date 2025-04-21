package Compiler.Src.AST.Node.StatementNode;

import java.util.ArrayList;

import Compiler.Src.AST.Node.DefNode.ASTVarDef;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTVarstatement extends ASTStatement {
    private final ArrayList<ASTVarDef> VarDefs;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
}
