package Compiler.Src.ASM.Node.Stmt;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.ASM.Node.Inst.ASMInst;

@lombok.Getter
@lombok.Setter
public class ASMStmt extends ASMNode {
    private ArrayList<ASMInst> insts;
    private ASMReg dest;

    public ASMStmt() {
        insts = new ArrayList<ASMInst>();
    }

    public void addInst(ASMInst inst) {
        insts.add(inst);
    }
    public void addInst(int offset,ASMInst inst) {
        insts.add(offset,inst);
    }

    public void appendInsts(ASMStmt nodes) {
        this.insts.addAll(nodes.getInsts());
    }

    public void appendInsts(int offset,ASMStmt nodes) {
        this.insts.addAll(offset,nodes.getInsts());
    }

    @Override
    public String toString() {
        String str = "";
        for (var inst : insts) {
            str += "    "+inst.toString() + "\n";
        }
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
