package Compiler.Src.AST.Node.StatementNode;

import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTForstatement extends ASTStatement implements ASTScopedNode {
    private LoopScope scope;
    private final ASTVarstatement Varinit;
    private final ASTExpr Exprinit, cond, step;
    private final ASTStatement stmts;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void addScope(BaseScope scope) {
        if (this.scope == null) {
            this.scope = new LoopScope(scope, null);
        }
    }
    // public LoopScope findscope() {
    //     return getScope();
    // }

    @Override
    public BaseScope getScope() {
        return scope;
    }
}
