package Compiler.Src.ASM.Allocator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import Compiler.Src.ASM.Entity.ASMPhysicalReg;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Node.Global.ASMFuncDef;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Memory.ASMLoad;
import Compiler.Src.ASM.Node.Inst.Memory.ASMStore;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMCall;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMRet;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.ASM.Util.BuiltInRegs;
import Compiler.Src.IR.Node.Stmt.IRBlock;
import Compiler.Src.Util.Error.OPTError;

public class LiveAnalysis {
    private TreeMap<String, ASMBlock> label2Block;
    BuiltInRegs BuiltInRegs;

    public void LiveAnalysisMethod(ASMFuncDef func) {
        BuiltInRegs = new BuiltInRegs();
        for (var block : func.getBlocks()) {
            init(block);
        }
        BuildCFG(func);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var block : func.getBlocks()) {
                changed |= CalcLive(block);
            }
        }
    }

    public void LiveAnalysisFinal(ASMFuncDef func) {
        BuiltInRegs = new BuiltInRegs();
        for (var block : func.getBlocks()) {
            initFinal(block);
        }
        BuildCFG(func);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var block : func.getBlocks()) {
                changed |= CalcLive(block);
            }
        }
    }

    public boolean CallRelated(ASMReg reg) {
        if (reg instanceof ASMPhysicalReg) {
            if (((ASMPhysicalReg) reg).equals(BuiltInRegs.getSp())
                    || ((ASMPhysicalReg) reg).equals(BuiltInRegs.getRa())
                    || ((ASMPhysicalReg) reg).equals(BuiltInRegs.getT0())) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void init(ASMBlock block) {
        block.uses = new HashSet<ASMReg>();
        block.def = new HashSet<ASMReg>();
        block.liveIn = new HashSet<ASMReg>();
        block.liveOut = new HashSet<ASMReg>();
        block.pred = new ArrayList<ASMBlock>();
        block.succ = new ArrayList<ASMBlock>();
        for (var inst : block.getInsts()) {
            // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
            // if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
            // continue;
            // }
            // if (inst instanceof ASMStore && CallRelated(((ASMStore)
            // inst).getUses().get(1))) {
            // continue;
            // }
            // }
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            // if(inst instanceof ASMCall)
            // {
            // ((ASMCall)inst).CallUses().forEach(reg -> {
            // if (!block.getDef().contains(reg)) {
            // block.getUses().add(reg);
            // }
            // });
            // }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
        for (var inst : block.getPhiStmt().getInsts()) {
            // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
            // if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
            // continue;
            // }
            // if (inst instanceof ASMStore && CallRelated(((ASMStore)
            // inst).getUses().get(1))) {
            // continue;
            // }
            // }
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            // if(inst instanceof ASMCall)
            // {
            // ((ASMCall)inst).CallUses().forEach(reg -> {
            // if (!block.getDef().contains(reg)) {
            // block.getUses().add(reg);
            // }
            // });
            // }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
        for (var inst : block.getReturnInst().getInsts()) {
            // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
            // if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
            // continue;
            // }
            // if (inst instanceof ASMStore && CallRelated(((ASMStore)
            // inst).getUses().get(1))) {
            // continue;
            // }
            // }
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            // if(inst instanceof ASMCall)
            // {
            // ((ASMCall)inst).CallUses().forEach(reg -> {
            // if (!block.getDef().contains(reg)) {
            // block.getUses().add(reg);
            // }
            // });
            // }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
    }

    public void initFinal(ASMBlock block) {
        block.uses = new HashSet<ASMReg>();
        block.def = new HashSet<ASMReg>();
        block.liveIn = new HashSet<ASMReg>();
        block.liveOut = new HashSet<ASMReg>();
        block.pred = new ArrayList<ASMBlock>();
        block.succ = new ArrayList<ASMBlock>();
        for (var inst : block.getInsts()) {
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            if (inst instanceof ASMCall) {
                ((ASMCall) inst).CallUses().forEach(reg -> {
                    if (!block.getDef().contains(reg)) {
                        block.getUses().add(reg);
                    }
                });
            }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
        for (var inst : block.getPhiStmt().getInsts()) {
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            if (inst instanceof ASMCall) {
                ((ASMCall) inst).CallUses().forEach(reg -> {
                    if (!block.getDef().contains(reg)) {
                        block.getUses().add(reg);
                    }
                });
            }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
        for (var inst : block.getReturnInst().getInsts()) {
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            if (inst instanceof ASMCall) {
                ((ASMCall) inst).CallUses().forEach(reg -> {
                    if (!block.getDef().contains(reg)) {
                        block.getUses().add(reg);
                    }
                });
            }
            if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                block.getDef().add(BuiltInRegs.getA0());
            }
            block.getDef().add(inst.getDef());
        }
    }

    public void BuildCFG(ASMFuncDef func) {
        label2Block = new TreeMap<>();
        for (var block : func.getBlocks()) {
            label2Block.put(block.getLabel().getLabel(), block);
        }
        for (var block : func.getBlocks()) {
            var size = block.getReturnInst().getInsts().size();
            var jumpInst = block.getReturnInst().getInsts().get(size - 1);
            if (jumpInst instanceof ASMJump) {
                var nextBlock = label2Block.get(((ASMJump) jumpInst).getLabel());
                block.addSucc(nextBlock);
                nextBlock.addPred(block);
            } else if (jumpInst instanceof ASMRet) {
                continue;
            } else {
                throw new OPTError("CFGBuilder: last inst is not jump");
            }
            if (size > 1) {
                var BeqzInst = block.getReturnInst().getInsts().get(size - 2);
                if (BeqzInst instanceof ASMBeq) {
                    var nextBlock = label2Block.get(((ASMBeq) BeqzInst).getLabel());
                    block.addSucc(nextBlock);
                    nextBlock.addPred(block);
                }
            }
        }
    }

    public boolean CalcLive(ASMBlock block) {
        HashSet<ASMReg> OldIn = new HashSet<ASMReg>(block.getLiveIn());
        HashSet<ASMReg> OldOut = new HashSet<ASMReg>(block.getLiveOut());

        block.setLiveIn(new HashSet<ASMReg>());
        block.setLiveOut(new HashSet<ASMReg>());
        for (var succ : block.getSucc()) {
            block.getLiveOut().addAll(succ.getLiveIn());
        }

        block.getLiveIn().addAll(block.getLiveOut());
        block.getLiveIn().removeAll(block.getDef());
        block.getLiveIn().addAll(block.getUses());

        return !block.getLiveIn().equals(OldIn) || !block.getLiveOut().equals(OldOut);
    }
}
