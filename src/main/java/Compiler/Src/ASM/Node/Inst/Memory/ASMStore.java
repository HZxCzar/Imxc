package Compiler.Src.ASM.Node.Inst.Memory;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMStore extends ASMInst {
    private String op;
    private ASMReg rs2, rs1;
    private int imm;

    public ASMStore(int id,ASMBlock parent,String op, ASMReg rs2, int imm, ASMReg rs1) {
        super(id, parent);
        this.op = op;
        this.rs2 = rs2;
        this.imm = imm;
        this.rs1 = rs1;
    }

    @Override
    public String toString() {
        String str = op + " " + rs2.toString() + ", " + imm + "(" + rs1.toString() + ")";
        return str;
    }
    @Override
    public void setDest(ASMReg reg) {
        return;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ASMReg getDef() {
        return null;
    }

    @Override
    public ArrayList<ASMReg> getUses() {
        var ret = new ArrayList<ASMReg>();
        if (rs1 instanceof ASMReg) {
            ret.add(rs1);
        }
        if (rs2 instanceof ASMReg) {
            ret.add(rs2);
        }
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        if (rs1.equals(oldReg)) {
            rs1 = newReg;
        }
        else if (rs2.equals(oldReg)) {
            rs2 = newReg;
        }
    }
}
