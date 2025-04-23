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
    protected ASTNode() {
        this.parent = null;
        this.pos = new position(0, 0);  // assuming position constructor requires row and column
    }

    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public String str() {
        return pos.str();
    }
}