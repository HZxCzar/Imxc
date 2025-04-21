package Compiler.Src.AST.Node.DefNode;

import Compiler.Src.AST.*;
import Compiler.Src.AST.Node.*;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;
import lombok.Getter;
import lombok.Setter;
@lombok.experimental.SuperBuilder
@Getter
@Setter
public class ASTDef extends ASTNode {
    private BaseInfo info;
    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }
    
    public String findName()
    {
        return getInfo().getName();
    }
}
