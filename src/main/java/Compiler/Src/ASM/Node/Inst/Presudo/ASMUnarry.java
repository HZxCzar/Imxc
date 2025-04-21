package Compiler.Src.ASM.Node.Inst.Presudo;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;

@lombok.Getter
@lombok.Setter
public class ASMUnarry extends ASMInst {
    private String op;
    private ASMReg dest, src;

    public ASMUnarry(int id,ASMBlock parent,String op, ASMReg dest, ASMReg src) {
        super(id, parent);
        this.op = op;
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String toString() {
        String str = op + " " + dest.toString() + ", " + src.toString();
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
        if (src instanceof ASMReg) {
            ret.add(src);
        }
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        if (src.equals(oldReg)) {
            src = newReg;
        }
    }
}
