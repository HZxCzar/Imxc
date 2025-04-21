package Compiler.Src.AST.Node.StatementNode;

import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.Error.*;
@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTIfstatement extends ASTStatement implements ASTScopedNode {
    private BaseScope ifScope,elseScope;
    private final ASTExpr judge;
    private final ASTStatement ifstmt,elsestmt;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void addScope(BaseScope scope) {
        if (this.ifScope == null) {
            this.ifScope = new BaseScope(scope,null);
        }
        if (this.elseScope == null) {
            this.elseScope = new BaseScope(scope,null);
        }
    }

    @Override
    public BaseScope getScope() {
        throw new ASTError("getScope() is not implemented for ifStmt");
    }
    public BaseScope getScope(String str) {
        if(str.equals("if"))
        {
            return ifScope;
        }
        else if(str.equals("else"))
        {
            return elseScope;
        }
        throw new ASTError("getScope() is not implemented for ifStmt");
    }

    public void addIfScope(BaseScope scope) {
        if (this.ifScope == null) {
            this.ifScope = new BaseScope(scope,null);
        }
    }

    public void addElseScope(BaseScope scope) {
        if (this.elseScope == null) {
            this.elseScope = new BaseScope(scope, null);
        }
    }

    public BaseScope getIfScope() {
        return ifScope;
    }

    public BaseScope getElseScope() {
        return elseScope;
    }
}
