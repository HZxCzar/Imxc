package Compiler.Src.ASM.Util;

import java.util.ArrayList;
import java.util.TreeMap;

import Compiler.Src.ASM.Entity.ASMPhysicalReg;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.ASM.Node.Global.ASMFuncDef;
import Compiler.Src.ASM.Node.Inst.Arithmetic.ASMArithI;
import Compiler.Src.ASM.Node.Inst.Arithmetic.ASMArithR;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Memory.ASMLoad;
import Compiler.Src.ASM.Node.Inst.Memory.ASMStore;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMLi;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMMove;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMRet;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Node.Stmt.ASMStmt;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.Util.Error.ASMError;

@lombok.Getter
@lombok.Setter
public class ASMControl {
    protected ASMCounter counter;
    protected BuiltInRegs regs;
    protected ASMBlock curBlock;
    protected ASMFuncDef curFunc;

    // mem2reg
    protected TreeMap<String, ASMBlock> label2block;
    protected ArrayList<ASMBlock> funcBlocks;
    protected int CreateblockCnt;

    protected ASMControl() {
        this.counter = new ASMCounter();
        this.regs = new BuiltInRegs();
        this.CreateblockCnt = 0;
    }

    public ASMPhysicalReg getArgReg(int i) {
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

    public ASMPhysicalReg getSReg(int i) {
        switch (i) {
            case 0:
                return regs.getS0();
            case 1:
                return regs.getS1();
            case 2:
                return regs.getS2();
            case 3:
                return regs.getS3();
            case 4:
                return regs.getS4();
            case 5:
                return regs.getS5();
            case 6:
                return regs.getS6();
            case 7:
                return regs.getS7();
            case 8:
                return regs.getS8();
            case 9:
                return regs.getS9();
            case 10:
                return regs.getS10();
            case 11:
                return regs.getS11();
            default:
                return null;
        }
    }

    public ASMPhysicalReg getTReg(int i) {
        switch (i) {
            case 0:
                return regs.getT0();
            case 1:
                return regs.getT1();
            case 2:
                return regs.getT2();
            case 3:
                return regs.getT3();
            case 4:
                return regs.getT4();
            case 5:
                return regs.getT5();
            case 6:
                return regs.getT6();
            default:
                return null;
        }
    }

    public ASMStmt StoreAt(ASMReg reg) {
        var InstList = new ASMStmt();
        InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getT1(), regs.getT1(), -4));
        // if (offset > 2047 || offset < -2048) {
        InstList.addInst(
                new ASMArithR(++ASMCounter.InstCount, curBlock, "add", regs.getT1(), regs.getSp(), regs.getT1()));
        InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "sw", reg, 0, regs.getT1()));
        // } else {
        // InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "sw", reg,
        // offset, regs.getSp()));
        // }
        // InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sub",
        // regs.getT0(), regs.getT0(), regs.getSp()));
        InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getT1(), regs.getT1(), 4));
        return InstList;
    }

    public ASMStmt LoadAt(ASMReg reg) {
        var InstList = new ASMStmt();
        InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getT1(), regs.getT1(), -4));
        // if (offset > 2047 || offset < -2048) {
        // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT0(),
        // offset));
        InstList.addInst(
                new ASMArithR(++ASMCounter.InstCount, curBlock, "add", regs.getT1(), regs.getSp(), regs.getT1()));
        InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "lw", reg, 0, regs.getT1()));
        // } else {
        // InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "sw", reg,
        // offset, regs.getSp()));
        // }
        // InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sub",
        // regs.getT0(), regs.getT0(), regs.getSp()));
        InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getT1(), regs.getT1(), 4));
        return InstList;
    }

    public int IRLiteral2Int(IRLiteral literal) {
        return literal.getValue().equals("null") ? 0 : Integer.parseInt(literal.getValue());
    }

    public boolean ValidImm(Object obj) {
        if (obj instanceof IRLiteral) {
            var value = IRLiteral2Int((IRLiteral) obj);
            if (value > -2048 && value < 2047) {
                return true;
            }
        }
        return false;
    }

    public void Formolize(ASMFuncDef func) {
        var initBlock = func.getBlocks().get(0);
        var total = (func.getStackSize() + 15) / 16 * 16;
        var initInst = new ASMStmt();
        curBlock = initBlock;
        initInst.addInst(0, new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT1(), total));
        initInst.addInst(1,
                new ASMArithR(++ASMCounter.InstCount, curBlock, "sub", regs.getSp(), regs.getSp(), regs.getT1()));
        initInst.appendInsts(StoreAt(regs.getRa()));

        initBlock.appendInsts(0, initInst);
        var jumpStmt = new ASMStmt();
        // if (func.getBlocks().size() > 1) {
            var jumpInst = new ASMJump(++ASMCounter.InstCount, curBlock,
                    func.getName() + "." + func.getBlocks().get(1).getLabel().getLabel());
            jumpStmt.addInst(jumpInst);
        // } else {
        //     jumpStmt.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT1(), total));
        //     jumpStmt.appendInsts(LoadAt(regs.getRa()));
        //     jumpStmt.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, regs.getSp(), regs.getT1()));
        // }
        initBlock.setReturnInst(jumpStmt);
        curBlock = null;
        for (int i = 1; i < func.getBlocks().size(); i++) {
            var curBlock = func.getBlocks().get(i);
            String Labelname_old = curBlock.getLabel().getLabel();
            String Labelname_new = func.getName() + "." + Labelname_old;
            curBlock.setLabel(new ASMLabel(Labelname_new));
            if (curBlock.getJlabel() != null) {
                String Jlabelname_old = curBlock.getJlabel().getLabel();
                String Jlabelname_new = func.getName() + "." + Jlabelname_old;
                curBlock.setJlabel(new ASMLabel(Jlabelname_new));
                curBlock.getJump().addFuncName(func.getName());
            }
            var size = curBlock.getReturnInst().getInsts().size();
            for (int j = 0; j < size; j++) {
                var inst = curBlock.getReturnInst().getInsts().get(j);
                if (inst instanceof ASMJump) {
                    ((ASMJump) inst).addFuncName(func.getName());
                }
                if (inst instanceof ASMBeq) {
                    ((ASMBeq) inst).addFuncName(func.getName());
                }
                if (inst instanceof ASMRet) {
                    if (j != size - 1) {
                        throw new ASMError("ret should be the last instruction in a block");
                    } else {
                        var returnInst = new ASMStmt();
                        returnInst.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT1(), total));
                        returnInst.appendInsts(LoadAt(regs.getRa()));
                        returnInst.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, regs.getSp(), regs.getT1()));
                        curBlock.getReturnInst().appendInsts(j, returnInst);
                    }
                    break;
                }
            }
        }
        curBlock = null;
    }
}
