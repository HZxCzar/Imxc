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

public class LiveAnalysis {

    HashMap<IRVariable, IRBlock> Var2Def;
    HashMap<IRVariable, HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>> Var2Use;
    HashSet<IRVariable> phidef;
    HashSet<Pair<IRBlock, IRVariable>> phiuse;
    IRFuncDef curFunc;
    // int number=0;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    public void work_on_func(IRFuncDef func) {
        HashMap<IRBlock, HashSet<IRVariable>> OldOut = new HashMap<IRBlock, HashSet<IRVariable>>();
        for (var block : func.getBlockstmts()) {
            OldOut.put(block, new HashSet<IRVariable>(block.getLiveOut()));
        }
        Var2Def = new HashMap<IRVariable, IRBlock>();
        Var2Use = new HashMap<IRVariable, HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>>();
        phidef = new HashSet<IRVariable>();
        phiuse = new HashSet<Pair<IRBlock, IRVariable>>();
        curFunc = func;
        Collect(func);
        // 要特判PHI的def
        work();
        // for (var block : func.getBlockstmts()) {
        //     for (var var : OldOut.get(block)) {
        //         if (!block.getLiveOut().contains(var) && !var.isGlobal()) {
        //             // throw new OPTError("LiveAnalysis Error");
        //             int a = 1;
        //         }
        //     }
        // }
        // for (var block : func.getBlockstmts()) {
        //     for (var var : block.getLiveOut()) {
        //         if (!OldOut.get(block).contains(var)) {
        //             int a = 1;
        //         }
        //     }
        // }
    }

    public void Collect(IRFuncDef func) {
        for (var block : func.getBlockstmts()) {
            block.liveIn = new HashSet<IRVariable>();
            block.liveInPhi = new HashMap<IRLabel, HashSet<IRVariable>>();
            block.liveOut = new HashSet<IRVariable>();
            block.uses = new HashSet<IRVariable>();
            block.def = new HashSet<IRVariable>();
            block.usesPhi = new HashMap<IRLabel, HashSet<IRVariable>>();
            block.defPhi = new HashMap<IRLabel, HashSet<IRVariable>>();

            for (var phi : block.getPhiList().values()) {
                Var2Def.put(phi.getDef(), block);
                phidef.add(phi.getDef());
                for (int i = 0; i < phi.getVals().size(); ++i) {
                    if (phi.getVals().get(i) instanceof IRLiteral) {
                        continue;
                    }
                    var val = (IRVariable) phi.getVals().get(i);
                    var label = phi.getLabels().get(i);
                    IRBlock pred = null;
                    for (var pre : block.getPredecessors()) {
                        if (pre.getLabelName().equals(label)) {
                            pred = pre;
                            break;
                        }
                    }
                    if (pred.getReturnInst() instanceof IRBranch && !((IRBranch) pred.getReturnInst()).isJump()) {
                        if (Var2Use.containsKey(val)) {
                            Var2Use.get(val).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(phi, false)));
                        } else {
                            Var2Use.put(val, new HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>());
                            Var2Use.get(val).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(phi, false)));
                        }
                        phiuse.add(new Pair<IRBlock, IRVariable>(block, val));
                    } else {
                        if (Var2Use.containsKey(val)) {
                            Var2Use.get(val).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(pred, new Pair<IRInst, Boolean>(phi, true)));
                        } else {
                            Var2Use.put(val, new HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>());
                            Var2Use.get(val).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(pred, new Pair<IRInst, Boolean>(phi, true)));
                        }
                    }
                }
            }
            for (var inst : block.getInsts()) {
                if (inst.getDef() != null) {
                    Var2Def.put(inst.getDef(), block);
                }
                for (var var : inst.getUses()) {
                    if (Var2Use.containsKey(var)) {
                        Var2Use.get(var).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(inst, false)));
                    } else {
                        Var2Use.put(var, new HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>());
                        Var2Use.get(var).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(inst, false)));
                    }
                }
            }
            for (var var : block.getReturnInst().getUses()) {
                if (Var2Use.containsKey(var)) {
                    Var2Use.get(var).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(block.getReturnInst(), false)));
                } else {
                    Var2Use.put(var, new HashSet<Pair<IRBlock, Pair<IRInst,Boolean>>>());
                    Var2Use.get(var).add(new Pair<IRBlock, Pair<IRInst,Boolean>>(block, new Pair<IRInst, Boolean>(block.getReturnInst(), false)));
                }
            }
        }
        for (var param : func.getParams()) {
            // if (Var2Use.containsKey(param)) {
            // Var2Use.get(param).add(func.getBlockstmts().get(0));
            // } else {
            // Var2Use.put(param, new HashSet<IRBlock>());
            // Var2Use.get(param).add(func.getBlockstmts().get(0));
            // }
            Var2Def.put(param, func.getBlockstmts().get(0));
        }
    }

    public void work() {
        for (IRVariable var : Var2Def.keySet()) {
            IRBlock def = Var2Def.get(var);
            HashSet<IRBlock> WorkList = new HashSet<IRBlock>();
            HashSet<IRBlock> Visited = new HashSet<IRBlock>();
            if (var.getValue().equals("%.tmp.binary.33")) {
                int a = 1;
            }
            if (!Var2Use.containsKey(var)) {
                continue;
            }
            for (var pair : Var2Use.get(var)) {
                var block = pair.a;
                if (block == def) {
                    if (phiuse.contains(new Pair<IRBlock, IRVariable>(block, var))) {
                        WorkList.add(block);
                        Visited.add(block);
                    }
                    continue;
                }
                for (var pred : block.getPredecessors()) {
                    if (pair.b.a instanceof IRPhi && pair.b.b == false) {
                        IRPhi phi = (IRPhi) pair.b.a;
                        for (int i = 0; i < phi.getVals().size(); ++i) {
                            if (phi.getVals().get(i).equals(var)) {
                                if (phi.getLabels().get(i).equals(pred.getLabelName())) {
                                    WorkList.add(pred);
                                    Visited.add(pred);
                                    break;
                                }
                            }
                        }
                    } else {
                        WorkList.add(pred);
                        Visited.add(pred);
                    }
                }
            }
            while (!WorkList.isEmpty()) {
                var block = WorkList.iterator().next();
                WorkList.remove(block);
                block.getLiveOut().add(var);
                if (block == def) {
                    continue;
                }
                for (var pred : block.getPredecessors()) {
                    if (!Visited.contains(pred)) {
                        WorkList.add(pred);
                        Visited.add(pred);
                    }
                }
            }
            if (phidef.contains(var)) {
                for (var pred : def.getPredecessors()) {
                    if (!Visited.contains(pred) && pred.getReturnInst() instanceof IRBranch
                            && ((IRBranch) pred.getReturnInst()).isJump()) {
                        pred.getLiveOut().add(var);
                    }
                }
            }
        }
        // var entry=curFunc.getBlockstmts().get(0);
        // for(var param:curFunc.getParams()){
        // entry.getLiveOut().add(param);
        // }
    }
}
