package Compiler.Src.ASM.Node.Global;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Node.ASMNode;

@lombok.Getter
@lombok.Setter
public class ASMStrDef extends ASMNode {
    private String name;
    private String value;

    public ASMStrDef(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        String str = name + ":\n";
        str += "    .string \"" + value + "\"\n";
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
