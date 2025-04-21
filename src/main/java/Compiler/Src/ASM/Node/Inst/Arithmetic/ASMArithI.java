package Compiler.Src.ASM.Node.Inst.Arithmetic;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMArithI extends ASMInst {
    private String op;
    private ASMReg dest, lhs;
    private int imm;

    public ASMArithI(int id,ASMBlock parent,String op, ASMReg dest, ASMReg lhs, int imm) {
        super(id, parent);
        this.op = op;
        this.dest = dest;
        this.lhs = lhs;
        this.imm = imm;
    }

    @Override
    public String toString() {
        String str = op + " " + dest.toString() + ", " + lhs.toString() + ", " + imm;
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
        if (lhs instanceof ASMReg) {
            ret.add(lhs);
        }
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        if (lhs.equals(oldReg)) {
            lhs = newReg;
        }
    }
}
