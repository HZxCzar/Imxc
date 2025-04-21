package Compiler.Src.AST.Node.StatementNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTBlockstatement extends ASTStatement implements ASTScopedNode {
    private BaseScope scope;
    private final ArrayList<ASTStatement> stmts;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void addScope(BaseScope scope) {
        if (this.scope == null) {
            this.scope = new BaseScope(scope, null);
        }
    }

    // public BaseScope findscope() {
    //     return getScope();
    // }

    @Override
    public BaseScope getScope() {
        return scope;
    }
}
