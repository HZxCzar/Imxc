package Compiler.Src.ASM.Node.Inst.Presudo;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMRet extends ASMInst {
    public ASMRet(int id, ASMBlock parent) {
        super(id, parent);
    }

    @Override
    public String toString() {
        String str = "ret";
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void setDest(ASMReg reg) {
        return;
    }

    @Override
    public ASMReg getDef() {
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
