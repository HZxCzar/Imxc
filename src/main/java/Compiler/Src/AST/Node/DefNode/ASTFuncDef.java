package Compiler.Src.AST.Node.DefNode;

import java.util.ArrayList;

import Compiler.Src.Util.Error.*;
import Compiler.Src.AST.Node.StatementNode.*;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.Info.FuncInfo;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.Util.Info.TypeInfo;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTFuncDef extends ASTDef implements ASTScopedNode {
    private FuncScope funcscope;
    private final ArrayList<ASTVarDef> params;
    private final ASTBlockstatement blockedBody;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void addScope(BaseScope scope) {
        if (this.funcscope == null) {
            this.funcscope = new FuncScope(scope, (FuncInfo) getInfo());
        }
    }

    @Override
    public BaseScope getScope() {
        return getFuncscope();
    }

    public TypeInfo getReturnType() {
        return ((FuncInfo) getInfo()).getFunctype();
    }
}
