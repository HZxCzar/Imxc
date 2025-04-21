package Compiler.Src.ASM.Node.Inst.Presudo;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMPhysicalReg;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.*;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Util.BuiltInRegs;

@lombok.Getter
@lombok.Setter
public class ASMCall extends ASMInst {
    private String funcName;
    private boolean hasReturnValue;
    private int ArgSize;
    private BuiltInRegs regs;

    public ASMCall(int id, ASMBlock parent, String funcName, boolean hasReturnValue, int ArgSize) {
        super(id, parent);
        this.funcName = funcName;
        this.hasReturnValue = hasReturnValue;
        this.ArgSize = ArgSize;
        this.regs = new BuiltInRegs();
    }

    @Override
    public String toString() {
        String str = "call " + funcName;
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

    public ArrayList<ASMReg> CallUses() {
        var ret = new ArrayList<ASMReg>();
        for (int i = 0; i < (ArgSize<8?ArgSize:8); i++) {
            ret.add(getA(i));
        }
        return ret;
    }

    public ASMPhysicalReg getA(int i) {
        switch (i) {
            case 0:
                return regs.getA0();
            case 1:
                return regs.getA1();
            case 2:
                return regs.getA2();
            case 3:
                return regs.getA3();
            case 4:
                return regs.getA4();
            case 5:
                return regs.getA5();
            case 6:
                return regs.getA6();
            case 7:
                return regs.getA7();
            default:
                return null;
        }
    }

    @Override
    public void replaceUse(ASMReg oldReg, ASMReg newReg) {
        return;
    }
}
