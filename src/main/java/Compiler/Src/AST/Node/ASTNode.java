package Compiler.Src.AST.Node;


import Compiler.Src.Util.position;
import Compiler.Src.AST.*;
import Compiler.Src.Util.Error.*;
import lombok.*;
@lombok.experimental.SuperBuilder
@Getter
@Setter
public class ASTNode {
    protected ASTNode parent;
    protected position pos;

    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public String str() {
        return pos.str();
    }
}