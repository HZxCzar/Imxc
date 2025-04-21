package Compiler.Src.ASM.Node;

import Compiler.Src.ASM.ASMVisitor;

public abstract class ASMNode {
    public abstract String toString();

    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
