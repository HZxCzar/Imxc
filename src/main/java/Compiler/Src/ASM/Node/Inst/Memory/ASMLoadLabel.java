package Compiler.Src.ASM.Node.Inst.Memory;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMLoadLabel extends ASMInst {
    ASMReg dest;
    String label;

    public ASMLoadLabel(int id,ASMBlock parent,ASMReg dest, String label) {
        super(id, parent);
        this.dest = dest;
        this.label = label;
    }

    @Override
    public String toString() {
        String str = "la " + dest.toString() + ", " + label;
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ASMReg getDef() {
        if (dest instanceof ASMReg) {
            return dest;
        }
        return null;
    }

    @Override
    public ArrayList<ASMReg> getUses() {
        var ret = new ArrayList<ASMReg>();
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        return;
    }
}
