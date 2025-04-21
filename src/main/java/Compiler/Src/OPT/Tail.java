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

public class Tail {
    private IRBlock curHead;
    private HashMap<IRVariable, IRVariable> args2new;
    private HashMap<IRVariable, HashSet<IRInst>> Var2Use;
    private HashMap<IRVariable, IRBlock> tailMap;
    private HashSet<IRVariable> WorkList;

    // public boolean jud(IRFuncDef func)
    // {
    //     if(func.getBlockstmts().size()>4000)
    //     {
    //         return true;
    //     }
    //     return false;
    // }

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    public void work_on_func(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        var entry = func.getBlockstmts().get(0);
        if (entry.getPhiList().size() != 0) {
            throw new OPTError("Tail: entry block has phi");
        }
        if (entry.getSuccessors().size() == 0) {
            return;
        } else if (entry.getSuccessors().size() > 1) {
            throw new OPTError("Tail: entry block has more than one successor");
        }
        // curHead = entry.getSuccessors().iterator().next();
        if (!check(func)) {
            return;
        }
        var succ = entry.getSuccessors().iterator().next();
        curHead = new IRBlock(new IRLabel("Tail", 0), 0);
        curHead.setReturnInst(entry.getReturnInst());
        func.getBlockstmts().add(1, curHead);
        entry.setReturnInst(new IRBranch(++InstCounter.InstCounter, curHead.getLabelName()));
        for (var phi : succ.getPhiList().values()) {
            for (int i = 0; i < phi.getVals().size(); i++) {
                if (phi.getLabels().get(i).equals(entry.getLabelName())) {
                    phi.getLabels().set(i, curHead.getLabelName());
                }
            }
        }
        entry.getSuccessors().remove(succ);
        entry.getSuccessors().add(curHead);
        curHead.getPredecessors().add(entry);
        curHead.getSuccessors().add(succ);
        succ.getPredecessors().add(curHead);
        succ.getPredecessors().remove(entry);
        InsertPhi(func);
        Ret2jump(func);
        Rename(func);
    }

    public boolean check(IRFuncDef func) {
        var entry = func.getBlockstmts().get(0);
        args2new = new HashMap<>();
        Var2Use = new HashMap<>();
        WorkList = new HashSet<IRVariable>();
        tailMap = new HashMap<>();
        for (var param : func.getParams()) {
            args2new.put(param, null);
        }
        for (var inst : entry.getInsts()) {
            for (var use : inst.getUses()) {
                if (args2new.containsKey(use)) {
                    throw new OPTError("Tail: entry block has use of param");
                }
            }
        }
        if (func.getReturnType().equals(GlobalScope.irVoidType)) {
            return false;
        }
        for (var arg : func.getParams()) {
            Var2Use.put(arg, new HashSet<IRInst>());
        }
        for (var block : func.getBlockstmts()) {
            for (var phiInst : block.getPhiList().values()) {
                if (Var2Use.get(phiInst.getDef()) == null) {
                    Var2Use.put(phiInst.getDef(), new HashSet<IRInst>());
                }
                for (var val : phiInst.getVals()) {
                    if (val instanceof IRVariable) {
                        if (Var2Use.get(val) == null) {
                            Var2Use.put((IRVariable) val, new HashSet<IRInst>());
                        }
                        Var2Use.get(val).add(phiInst);
                    }
                }
            }
            for (var inst : block.getInsts()) {
                for (var use : inst.getUses()) {
                    if (Var2Use.get(use) == null) {
                        Var2Use.put((IRVariable) use, new HashSet<IRInst>());
                    }
                    Var2Use.get(use).add(inst);
                }
                if (inst.getDef() != null) {
                    if (Var2Use.get(inst.getDef()) == null) {
                        Var2Use.put(inst.getDef(), new HashSet<IRInst>());
                    }
                }
            }
            for (var use : block.getReturnInst().getUses()) {
                if (Var2Use.get(use) == null) {
                    Var2Use.put((IRVariable) use, new HashSet<IRInst>());
                }
            }
        }
        for (var block : func.getBlockstmts()) {
            if (block.getInsts().size() >= 1 && block.getInsts().get(block.getInsts().size() - 1) instanceof IRCall) {
                IRCall call = (IRCall) block.getInsts().get(block.getInsts().size() - 1);
                if (block.getReturnInst() instanceof IRRet
                        && ((IRRet) block.getReturnInst()).getUses().contains(call.getDef()) && call.getFuncName().equals(func.getName())) {
                    WorkList.add(call.getDef());
                    tailMap.put(call.getDef(), block);
                }
            }
        }
        return WorkList.size() == 0 ? false : true;
    }

    public void Ret2jump(IRFuncDef func) {
        for (var val : WorkList) {
            var block = tailMap.get(val);
            block.setReturnInst(new IRBranch(++InstCounter.InstCounter, curHead.getLabelName()));
            curHead.getPredecessors().add(block);
            block.getSuccessors().add(curHead);
            var callInst = (IRCall) tailMap.get(val).getInsts().get(tailMap.get(val).getInsts().size() - 1);
            if (!callInst.getDef().equals(val)) {
                throw new OPTError("Tail: callInst.getDef()!=val");
            }
            tailMap.get(val).getInsts().remove(callInst);
            for (int i = callInst.getArgs().size() > func.getParams().size() ? 1 : 0; i < callInst.getArgs()
                    .size(); i++) {
                if(callInst.getArgs().size() > func.getParams().size())
                {
                    int a=1;
                }
                var originArg = func.getParams().get(i);
                var phiInst = curHead.getPhiList().get(args2new.get(originArg));
                phiInst.getVals().add(callInst.getArgs().get(i));
                phiInst.getLabels().add(block.getLabelName());
            }
        }
    }

    public void InsertPhi(IRFuncDef func) {
        for (var arg : args2new.keySet()) {
            IRVariable newArg = new IRVariable(arg.getType(), arg.getValue() + ".tail");
            args2new.put(arg, newArg);
            var vals = new ArrayList<IREntity>();
            vals.add(arg);
            var labels = new ArrayList<IRLabel>();
            labels.add(func.getBlockstmts().get(0).getLabelName());
            curHead.getPhiList().put(newArg,
                    new IRPhi(++InstCounter.InstCounter, newArg, newArg.getType(), vals, labels));
        }
    }

    public void Rename(IRFuncDef func) {
        for (var block : func.getBlockstmts()) {
            if (block == curHead) {
                for (var phiInst : block.getPhiList().values()) {
                    for (int i = 0; i < phiInst.getVals().size(); i++) {
                        if (!phiInst.getLabels().get(i).equals(func.getBlockstmts().get(0).getLabelName()) && args2new.containsKey(phiInst.getVals().get(i))) {
                            phiInst.getVals().set(i, args2new.get(phiInst.getVals().get(i)));
                        }
                    }
                }
                continue;
            }
            for (var phiInst : block.getPhiList().values()) {
                for (int i = 0; i < phiInst.getVals().size(); i++) {
                    if (args2new.containsKey(phiInst.getVals().get(i))) {
                        phiInst.getVals().set(i, args2new.get(phiInst.getVals().get(i)));
                    }
                }
            }
            for (var inst : block.getInsts()) {
                for (var use : inst.getUses()) {
                    if (args2new.containsKey(use)) {
                        inst.replaceUse(use, args2new.get(use));
                    }
                }
            }
            for (var use : block.getReturnInst().getUses()) {
                if (args2new.containsKey(use)) {
                    block.getReturnInst().replaceUse(use, args2new.get(use));
                }
            }
        }
    }
}
