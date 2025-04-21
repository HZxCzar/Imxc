package Compiler.Src.ASM.Entity;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
public abstract class ASMReg implements Comparable<ASMReg> {
    protected final String name;

    public ASMReg(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASMReg) {
            return ((ASMReg) obj).name.equals(this.name);
        }
        return false;
    }

    @Override
    public abstract int compareTo(ASMReg o);
    //     return name.compareTo(o.name);
    // }

    @Override
    public abstract int hashCode();
}
