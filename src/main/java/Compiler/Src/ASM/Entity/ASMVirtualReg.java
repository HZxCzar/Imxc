package Compiler.Src.ASM.Entity;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
public class ASMVirtualReg extends ASMReg{
    public static int allocaCount = 0;
    private int id;

    public ASMVirtualReg(int id) {
        super("ASMVirtualReg");
        this.id = id;
    }

    // public ASMVirtualReg(ASMCounter counter) {
    //     super("ASMVirtualReg");
    //     this.offset = (counter.allocaCount++)-2;
    // }

    // @Override
    // public String toString() {
    //     throw new ASMError("Virtual register should not be printed");
    // }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASMVirtualReg) {
            return id==((ASMVirtualReg) obj).id;
        }
        return false;
    }

    @Override
    public int compareTo(ASMReg o) {
        int nameComparison = this.getName().compareTo(o.getName());
        if (nameComparison != 0) {
            return nameComparison;
        }
        return Integer.compare(this.id, ((ASMVirtualReg)o).id);
    }

    @Override
    public int hashCode() {
            return Objects.hash(name, ((ASMVirtualReg) this).getId());
    }
}
