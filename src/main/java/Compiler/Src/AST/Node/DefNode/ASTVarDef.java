package Compiler.Src.AST.Node.DefNode;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.Util.Info.VarInfo;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTVarDef extends ASTDef {
    private final ASTExpr initexpr;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    public TypeInfo getVarType() {
        return ((VarInfo) getInfo()).getType();
    }
}
