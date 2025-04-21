package Compiler.Src.ASM.Node.Inst.Arithmetic;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMArithR extends ASMInst {
    private String op;
    private ASMReg dest, lhs, rhs;
    public ASMArithR(int id,ASMBlock parent,String op, ASMReg dest, ASMReg lhs, ASMReg rhs) {
        super(id, parent);
        this.op = op;
        this.dest = dest;
        this.lhs = lhs;
        this.rhs = rhs;
    }
    @Override
    public String toString() {
        String str = op + " " + dest.toString() + ", " + lhs.toString() + ", " + rhs.toString();
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
        if(rhs instanceof ASMReg) {
            ret.add(rhs);
        }
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        if (lhs.equals(oldReg)) {
            lhs = newReg;
        }
        else if(rhs.equals(oldReg)) {
            rhs = newReg;
        }
    }
}
