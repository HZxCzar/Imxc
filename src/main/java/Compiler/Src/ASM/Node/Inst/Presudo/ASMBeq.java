package Compiler.Src.ASM.Node.Inst.Presudo;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.Util.Error.ASMError;

@lombok.Getter
@lombok.Setter
public class ASMBeq extends ASMInst {
    private ASMReg rs1, rs2;
    private String Label;
    private int type;

    public ASMBeq(int id, ASMBlock parent, ASMReg rs1, ASMReg rs2, String label, int type) {
        super(id, parent);
        this.rs1 = rs1;
        this.rs2 = rs2;
        Label = label;
        this.type = type;
    }

    @Override
    public String toString() {
        String str;
        if (type == 0) {
            str = "beqz " + rs1.toString() + ", " + Label;
        } else if (type == 1) {
            str = "beq " + rs1.toString() + ", " + rs2.toString()+"," + Label;
        } else if (type == 2) {
            str = "bne " + rs1.toString() + ", " + rs2.toString()+"," + Label;
        } else {
            throw new ASMError("ASMBeq type error");
        }
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public void addFuncName(String funcName) {
        Label = funcName + "." + Label;
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
        if (rs1 instanceof ASMReg) {
            ret.add(rs1);
        }
        if (rs2!=null) {
            ret.add(rs2);
        }
        return ret;
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        if (rs1.equals(oldReg)) {
            rs1 = newReg;
        }
        if (rs2!=null && rs2.equals(oldReg)) {
            rs2 = newReg;
        }
    }
}
