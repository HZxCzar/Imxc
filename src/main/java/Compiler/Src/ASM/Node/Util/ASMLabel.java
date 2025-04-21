package Compiler.Src.ASM.Node.Util;

@lombok.Getter
@lombok.Setter
public class ASMLabel{
    private final String label;

    public ASMLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // @Override
    // public <T> T accept(ASMVisitor<T> visitor) {
    //     return visitor.visit(this);
    // }

    // @Override
    // public ASMVirtualReg getDef() {
    //     return null;
    // }

    // @Override
    // public ArrayList<ASMVirtualReg> getUses() {
    //     var ret = new ArrayList<ASMVirtualReg>();
    //     return ret;
    // }
}
