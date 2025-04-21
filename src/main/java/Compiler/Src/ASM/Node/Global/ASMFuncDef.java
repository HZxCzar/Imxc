package Compiler.Src.ASM.Node.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.ASM.Node.Inst.Arithmetic.*;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Memory.ASMLoad;
import Compiler.Src.ASM.Node.Inst.Memory.ASMStore;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMLi;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMRet;
import Compiler.Src.ASM.Node.Stmt.*;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.ASM.Util.ASMControl;
import Compiler.Src.ASM.Util.ASMCounter;
import Compiler.Src.Util.Error.ASMError;

@lombok.Getter
@lombok.Setter
public class ASMFuncDef extends ASMNode {
    private String name;
    private int paramCount;
    private ArrayList<ASMBlock> blocks;
    private ArrayList<ASMBlock> order2Block;

    public int StackSize;
    public ASMVirtualReg topPointer;

    public HashMap<ASMReg, Integer> color;

    public ASMFuncDef(String name, int paramCount) {
        this.name = name;
        this.paramCount = paramCount;
        this.blocks = new ArrayList<ASMBlock>();
        this.order2Block = new ArrayList<ASMBlock>();
        this.StackSize = 0;
    }

    public void addBlock(ASMBlock block) {
        blocks.add(block);
    }

    @Override
    public String toString() {
        // String str = " .global" + name + "\n";
        String str = ".globl " + name + "\n";
        for (var block : blocks) {
            str += block.toString() + "\n";
        }
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
