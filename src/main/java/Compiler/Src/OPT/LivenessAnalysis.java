package Compiler.Src.OPT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMCall;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Util.BuiltInRegs;
import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.IRNode;
import Compiler.Src.IR.Node.IRRoot;
import Compiler.Src.IR.Node.Def.IRFuncDef;
import Compiler.Src.IR.Node.Def.IRGlobalDef;
import Compiler.Src.IR.Node.Def.IRStrDef;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.IRBlock;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.Type.IRStructType;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

public class LivenessAnalysis {
    HashMap<Pair<IRBlock, IRLabel>, HashSet<IRVariable>> Block2Phi;
    HashMap<Pair<IRBlock, IRLabel>, HashSet<IRVariable>> Block2Use;
    IRBlock entrBlock;
    IRFuncDef curFunc;
    // int number=0;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    public void work_on_func(IRFuncDef func) {
        Block2Phi = new HashMap<Pair<IRBlock, IRLabel>, HashSet<IRVariable>>();
        Block2Use = new HashMap<Pair<IRBlock, IRLabel>, HashSet<IRVariable>>();
        curFunc = func;
        entrBlock = func.getBlockstmts().get(0);
        for (var block : func.getBlockstmts()) {
            initPhi(block);
        }
        for (var block : func.getBlockstmts()) {
            init(block);
        }
        // for (int i = 0; i < 2; ++i) {
            boolean changed = true;
            while (changed) {
                changed = false;
                for (var block : func.getBlockstmts()) {
                    changed |= CalcLive(block);
                    // ++number;
                    // System.out.println(number);
                }
            }
        // }
    }

    public void init(IRBlock block) {
        block.uses = new HashSet<IRVariable>();
        block.def = new HashSet<IRVariable>();
        if (block == entrBlock) {
            for (var arg : curFunc.getParams()) {
                block.getDef().add(arg);
            }
        }
        for (var inst : block.getInsts()) {
            inst.getUses().forEach(reg -> {
                if (!block.getDef().contains(reg)) {
                    block.getUses().add(reg);
                }
            });
            block.getDef().add(inst.getDef());
        }
        for (var use : block.getReturnInst().getUses()) {
            if (!block.getDef().contains(use)) {
                block.getUses().add(use);
            }
        }
        if (block.getReturnInst() instanceof IRBranch && ((IRBranch) block.getReturnInst()).isJump()) {
            var succ = block.getSuccessors().iterator().next();
            var pair = new Pair<IRBlock, IRLabel>(succ, block.getLabelName());
            for (var use : Block2Use.get(pair)) {
                if (!block.getDef().contains(use)) {
                    block.getUses().add(use);
                }
                for (var pred : block.getPredecessors()) {
                    if (!block.getDefPhi().get(pred.getLabelName()).contains(use)) {
                        block.getUsesPhi().get(pred.getLabelName()).add(use);
                    }
                }
            }
            block.getDef().addAll(Block2Phi.get(pair));
            for (var pred : block.getPredecessors()) {
                block.getDefPhi().get(pred.getLabelName()).addAll(Block2Phi.get(pair));
            }
        }
    }

    public void initPhi(IRBlock block) {
        block.usesPhi = new HashMap<IRLabel, HashSet<IRVariable>>();
        block.defPhi = new HashMap<IRLabel, HashSet<IRVariable>>();
        for (var pred : block.getPredecessors()) {
            block.usesPhi.put(pred.getLabelName(), new HashSet<IRVariable>());
            block.defPhi.put(pred.getLabelName(), new HashSet<IRVariable>());
        }
        if (block == entrBlock) {
            for (var arg : curFunc.getParams()) {
                for (var pred : block.getPredecessors()) {
                    block.getDefPhi().get(pred.getLabelName()).add(arg);
                }
            }
        }
        for (var pred : block.getPredecessors()) {
            Block2Phi.put(new Pair<IRBlock, IRLabel>(block, pred.getLabelName()), new HashSet<IRVariable>());
            Block2Use.put(new Pair<IRBlock, IRLabel>(block, pred.getLabelName()), new HashSet<IRVariable>());
        }
        for (var phi : block.getPhiList().values()) {
            for (int i = 0; i < phi.getVals().size(); i++) {
                var val = phi.getVals().get(i);
                var label = phi.getLabels().get(i);
                if (val instanceof IRVariable) {
                    block.getUsesPhi().get(label).add((IRVariable) val);
                    Block2Use.get(new Pair<IRBlock, IRLabel>(block, label)).add((IRVariable) val);
                }
            }
        }
        // Block2Phi.put(block, new HashSet<IRVariable>());
        for (var phi : block.getPhiList().values()) {
            for (var def : block.getDefPhi().keySet()) {
                if (!phi.getLabels().contains(def)) {
                    throw new OPTError("Phi not defined in all predecessors");
                }
                block.getDefPhi().get(def).add(phi.getDef());
                Block2Phi.get(new Pair<IRBlock, IRLabel>(block, def)).add(phi.getDef());
            }
            // Block2Phi.get(block).add(phi.getDef());
        }
        for (var inst : block.getInsts()) {
            for (var pred : block.getPredecessors()) {
                inst.getUses().forEach(reg -> {
                    if (!block.getDefPhi().get(pred.getLabelName()).contains(reg)) {
                        block.getUsesPhi().get(pred.getLabelName()).add(reg);
                    }
                });
                block.getDefPhi().get(pred.getLabelName()).add(inst.getDef());
            }
        }
        for (var use : block.getReturnInst().getUses()) {
            for (var pred : block.getPredecessors()) {
                if (!block.getDefPhi().get(pred.getLabelName()).contains(use)) {
                    block.getUsesPhi().get(pred.getLabelName()).add(use);
                }
            }
        }
    }

    public boolean CalcLive(IRBlock block) {
        var returnInst = block.getReturnInst();
        if (returnInst instanceof IRBranch && !((IRBranch) returnInst).isJump()) {
            HashSet<IRVariable> OldIn = new HashSet<IRVariable>(block.getLiveIn());
            HashMap<IRLabel, HashSet<IRVariable>> OldInPhi = new HashMap<IRLabel, HashSet<IRVariable>>(
                    block.getLiveInPhi());
            HashSet<IRVariable> OldOut = new HashSet<IRVariable>(block.getLiveOut());

            block.setLiveIn(new HashSet<IRVariable>());
            block.setLiveInPhi(new HashMap<IRLabel, HashSet<IRVariable>>());
            block.setLiveOut(new HashSet<IRVariable>());
            for (var succ : block.getSuccessors()) {
                if(succ==block){
                    if(OldInPhi.get(block.getLabelName())==null){
                        continue;
                    }
                    block.getLiveOut().addAll(OldInPhi.get(block.getLabelName()));
                }
                if (succ.getLiveInPhi() == null) {
                    continue;
                }
                if (succ.getLiveInPhi().get(block.getLabelName()) == null) {
                    continue;
                }
                block.getLiveOut().addAll(succ.getLiveInPhi().get(block.getLabelName()));
            }

            block.getLiveIn().addAll(block.getLiveOut());
            block.getLiveIn().removeAll(block.getDef());
            block.getLiveIn().addAll(block.getUses());

            for (var pred : block.getPredecessors()) {
                block.getLiveInPhi().put(pred.getLabelName(), new HashSet<IRVariable>());
                block.getLiveInPhi().get(pred.getLabelName()).addAll(block.getLiveOut());
                block.getLiveInPhi().get(pred.getLabelName()).removeAll(block.getDefPhi().get(pred.getLabelName()));
                block.getLiveInPhi().get(pred.getLabelName()).addAll(block.getUsesPhi().get(pred.getLabelName()));
            }

            // block.getLiveInPhi().addAll(block.getLiveOut());
            // block.getLiveInPhi().removeAll(block.getDefPhi());
            // block.getLiveInPhi().addAll(block.getUsesPhi());

            return !block.getLiveIn().equals(OldIn) || !block.getLiveOut().equals(OldOut)
                    || !block.getLiveInPhi().equals(OldInPhi);
        } else {
            HashSet<IRVariable> OldIn = new HashSet<IRVariable>(block.getLiveIn());
            HashMap<IRLabel, HashSet<IRVariable>> OldInPhi = new HashMap<IRLabel, HashSet<IRVariable>>(
                    block.getLiveInPhi());
            HashSet<IRVariable> OldOut = new HashSet<IRVariable>(block.getLiveOut());

            block.setLiveIn(new HashSet<IRVariable>());
            block.setLiveInPhi(new HashMap<IRLabel, HashSet<IRVariable>>());
            block.setLiveOut(new HashSet<IRVariable>());
            for (var succ : block.getSuccessors()) {
                block.getLiveOut().addAll(succ.getLiveIn());
            }

            block.getLiveIn().addAll(block.getLiveOut());
            block.getLiveIn().removeAll(block.getDef());
            block.getLiveIn().addAll(block.getUses());

            for (var pred : block.getPredecessors()) {
                block.getLiveInPhi().put(pred.getLabelName(), new HashSet<IRVariable>());
                block.getLiveInPhi().get(pred.getLabelName()).addAll(block.getLiveOut());
                block.getLiveInPhi().get(pred.getLabelName()).removeAll(block.getDefPhi().get(pred.getLabelName()));
                block.getLiveInPhi().get(pred.getLabelName()).addAll(block.getUsesPhi().get(pred.getLabelName()));
            }

            // block.getLiveInPhi().addAll(block.getLiveOut());
            // block.getLiveInPhi().removeAll(block.getDefPhi());
            // block.getLiveInPhi().addAll(block.getUsesPhi());

            return !block.getLiveIn().equals(OldIn) || !block.getLiveOut().equals(OldOut)
                    || !block.getLiveInPhi().equals(OldInPhi);
        }
    }
}
