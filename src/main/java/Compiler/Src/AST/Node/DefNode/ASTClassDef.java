package Compiler.Src.AST.Node.DefNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTClassDef extends ASTDef implements ASTScopedNode {
    private ClassScope classScope;
    private ASTFuncDef constructor;
    private ArrayList<ASTVarDef> vars;
    private ArrayList<ASTFuncDef> funcs;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public void addScope(BaseScope scope) {
        if (this.classScope == null) {
            this.classScope = new ClassScope(scope, (ClassInfo) getInfo());
        }
    }

    @Override
    public BaseScope getScope() {
        return getClassScope();
    }

    // public ClassScope findScope() {
    //     return classScope;
    // }
}
