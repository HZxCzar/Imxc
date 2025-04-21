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

public class RovB {
    public void visit(IRRoot root) {
        new PhiRep().visit(root);
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> rmvSingle(func));
        root.getFuncs().forEach(func -> rmvSucc(func));
    }
    // public boolean jud(IRFuncDef func)
    // {
    //     if(func.getBlockstmts().size()>4000)
    //     {
    //         return true;
    //     }
    //     return false;
    // }

    public void rmvSingle(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        var change = true;
        while (change) {
            change = false;
            var deadBlock = new HashSet<IRBlock>();
            for (var block : func.getBlockstmts()) {
                if (block.getInsts().size() == 0 && block.getPredecessors().size() == 1
                        && block.getSuccessors().size() == 1) {
                    if (block.getPhiList().size() != 0) {
                        throw new OPTError("RemoveBlock: phiList is not empty");
                    }
                    var label = block.getLabelName();
                    var pred = block.getPredecessors().iterator().next();
                    var succ = block.getSuccessors().iterator().next();
                    if (!(pred.getReturnInst() instanceof IRBranch)) {
                        throw new OPTError("RemoveBlock: returnInst is not IRBranch");
                    }
                    var branchInst = (IRBranch) pred.getReturnInst();
                    if (!branchInst.isJump()) {
                        if (branchInst.getTrueLabel().equals(label)
                                && branchInst.getFalseLabel().equals(succ.getLabelName())) {
                            continue;
                        } else if (branchInst.getFalseLabel().equals(label)
                                && branchInst.getTrueLabel().equals(succ.getLabelName())) {
                            continue;
                        }
                    }
                    if (branchInst.getTrueLabel().equals(label)) {
                        branchInst.setTrueLabel(succ.getLabelName());
                    } else if (branchInst.getFalseLabel().equals(label)) {
                        branchInst.setFalseLabel(succ.getLabelName());
                    } else {
                        throw new OPTError("RemoveBlock: label not match");
                    }
                    deadBlock.add(block);
                    pred.getSuccessors().remove(block);
                    pred.getSuccessors().add(succ);
                    succ.getPredecessors().remove(block);
                    succ.getPredecessors().add(pred);
                    for (var phi : succ.getPhiList().values()) {
                        for (int i = 0; i < phi.getLabels().size(); i++) {
                            if (phi.getLabels().get(i).equals(label)) {
                                phi.getLabels().remove(i);
                                phi.getLabels().add(i, pred.getLabelName());
                            }
                        }
                    }
                }
            }
            for (var dead : deadBlock) {
                change = true;
                func.getBlockstmts().remove(dead);
            }
        }
    }

    public void rmvSucc(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        var change = true;
        while (change) {
            change = false;
            var deadBlock = new HashSet<IRBlock>();
            for (var block : func.getBlockstmts()) {
                if (block.getPredecessors().size() == 1
                        && block.getPredecessors().iterator().next().getSuccessors().size() == 1) {
                    if (block.getLabelName().getLabel().equals("loop.3.endLabel")) {
                        int a = 1;
                    }
                    if (block.getPhiList().size() != 0) {
                        throw new OPTError("RemoveBlock: phiList is not empty");
                    }
                    var label = block.getLabelName();
                    var pred = block.getPredecessors().iterator().next();
                    deadBlock.add(block);
                    var insts = block.getInsts();
                    for (var inst : insts) {
                        pred.addInsts(inst);
                    }
                    pred.getSuccessors().remove(block);
                    pred.getSuccessors().addAll(block.getSuccessors());
                    pred.setReturnInst(block.getReturnInst());
                    for (var succ : block.getSuccessors()) {
                        succ.getPredecessors().remove(block);
                        succ.getPredecessors().add(pred);
                        for (var phi : succ.getPhiList().values()) {
                            for (int i = 0; i < phi.getLabels().size(); i++) {
                                if (phi.getLabels().get(i).equals(label)) {
                                    phi.getLabels().remove(i);
                                    phi.getLabels().add(i, pred.getLabelName());
                                }
                            }
                        }
                    }
                }
            }
            for (var dead : deadBlock) {
                change = true;
                func.getBlockstmts().remove(dead);
            }
        }
    }
}
