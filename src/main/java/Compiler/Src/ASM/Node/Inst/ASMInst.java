package Compiler.Src.ASM.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.Util.Error.ASMError;

@lombok.Getter
@lombok.Setter
public abstract class ASMInst extends ASMNode {
    protected ASMBlock parent=null;
    protected int id;
    public ASMInst(int id, ASMBlock parent) {
        this.id = id;
        this.parent = parent;
    }

    @Override
    public String toString() {
        throw new ASMError("Instruction should not be printed");
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public abstract void setDest(ASMReg reg);

    public abstract ASMReg getDef();

    public abstract ArrayList<ASMReg> getUses();

    public abstract void replaceUse(ASMReg oldReg, ASMReg newReg);
}
