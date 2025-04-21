package Compiler.Src.AST.Node.Util;

import Compiler.Src.Util.ScopeUtil.BaseScope;

public interface  ASTScopedNode {
    BaseScope getScope();
    void addScope(BaseScope scope);
}
