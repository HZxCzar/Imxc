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

public class ADCE {

    // private IRFuncDef currentFunc;
    private Stack<IRBlock> WorkStack = new Stack<IRBlock>();

    private ArrayList<IRBlock> PostOrder = new ArrayList<IRBlock>();
    private HashSet<IRBlock> visited = new HashSet<IRBlock>();
    private HashSet<IRInst> WorkList;
    private HashMap<IRVariable, IRInst> var2def;
    // private HashMap<String, IRInst> label2jump;
    private HashSet<IRInst> Live;
    private HashSet<IRBlock> LiveBlock;
    private HashMap<IRInst, IRBlock> inst2block;
    private HashMap<IRLabel, IRBlock> name2block;

    // public boolean jud(IRFuncDef func) {
    //     if (func.getBlockstmts().size() > 4000) {
    //         return true;
    //     }
    //     return false;
    // }

    public OPTError visit(IRRoot root) throws OPTError {
        new CFGBuilder().visit(root);
        for (var func : root.getFuncs()) {
            // if (jud(func)) {
            //     continue;
            // }
            WorkList = new HashSet<>();
            var2def = new HashMap<>();
            Live = new HashSet<>();
            LiveBlock = new HashSet<>();
            inst2block = new HashMap<>();
            name2block = new HashMap<>();
            PostOrder = new ArrayList<>();
            visited = new HashSet<>();
            // label2jump = new HashMap<>();
            buildDom(func);
            Collect(func);
            Work();
            Update(func);
        }
        // new CFGBuilder().visit(root);
        return new OPTError();
    }

    public void buildDom(IRFuncDef func) {
        var entryBlock = new IRBlock(new IRLabel("end", 0), 0);
        func.setEndBlock(entryBlock);
        WorkStack = new Stack<IRBlock>();
        for (var block : func.getBlockstmts()) {
            if (block.getReturnInst() instanceof IRRet) {
                entryBlock.getPredecessors().add(block);
                block.setRidom(entryBlock);
            }
        }
        CalcRpo(entryBlock);
        entryBlock.setRidom(entryBlock);
        entryBlock.getSuccessors().add(entryBlock);
        for(int i=0;i<func.getBlockstmts().size();++i)
        {
            var block=func.getBlockstmts().get(i);
            if(!PostOrder.contains(block))
            {
                func.getBlockstmts().remove(i);
                --i;
            }
        }
        // build DomTree
        boolean run = true;
        while (run) {
            run = false;
            for (int i = PostOrder.size() - 1; i >= 0; i--) {
                var block = PostOrder.get(i);
                if (entryBlock.getPredecessors().contains(block)) {
                    continue;
                }
                if (calcRidom(block)) {
                    run = true;
                }
            }
        }

        // build DomFrontier
        for (int i = PostOrder.size() - 1; i >= 0; i--) {
            var block = PostOrder.get(i);
            if (block.getRidom() != block && block.getRidom() != entryBlock) {
                block.getRidom().getRDomChildren().add(block);
            }
            calcDF(block);
        }
        return;
    }

    public void calcDF(IRBlock block) {
        for (var pred : block.getSuccessors()) {
            var runner = pred;
            while (runner != block.getRidom()) // entry?
            {
                runner.getRDomFrontier().add(block);
                runner = runner.getRidom();
            }
        }
    }

    public void CalcRpo(IRBlock block) {
        visited.add(block);
        WorkStack.push(block);
        Boolean run = true;
        while (!WorkStack.empty()) {
            run = false;
            var cur = WorkStack.peek();
            for (var succ : cur.getPredecessors()) {
                if (!visited.contains(succ)) {
                    visited.add(succ);
                    WorkStack.push(succ);
                    run = true;
                }
            }
            if (run) {
                continue;
            } else {
                var postblock = WorkStack.pop();
                PostOrder.add(postblock);
                // currentFunc.getOrder2Block().add(0, postblock);
            }
        }
        // PostOrder.add(block);
        // currentFunc.getOrder2Block().add(0, block);
    }

    public boolean calcRidom(IRBlock block) {
        IRBlock newRidom = null;
        for (var pred : block.getSuccessors()) {
            if (newRidom == null && pred.getRidom() != null) {
                newRidom = pred;
            } else if (pred.getRidom() != null) {
                newRidom = intersect(pred, newRidom);
            }
        }
        if (block.getRidom() != newRidom) {
            block.setRidom(newRidom);
            return true;
        }
        return false;
    }

    public IRBlock intersect(IRBlock b1, IRBlock b2) { // LCA
        while (b1 != b2) {
            while (PostOrder.indexOf(b1) < PostOrder.indexOf(b2)) {
                b1 = b1.getRidom();
            }
            while (PostOrder.indexOf(b1) > PostOrder.indexOf(b2)) {
                b2 = b2.getRidom();
            }
        }
        return b1;
    }

    public void Collect(IRFuncDef func) throws OPTError {
        for (var block : func.getBlockstmts()) {
            name2block.put(block.getLabelName(), block);
            for (var inst : block.getPhiList().values()) {
                var2def.put(((IRPhi) inst).getDef(), inst);
                inst2block.put(inst, block);
            }
            for (var inst : block.getInsts()) {
                var2def.put(inst.getDef(), inst);
                inst2block.put(inst, block);
                if (SideEffect(inst)) {
                    WorkList.add(inst);
                }
            }
            if (SideEffect(block.getReturnInst())) {
                WorkList.add(block.getReturnInst());
            }
            inst2block.put(block.getReturnInst(), block);
        }
    }

    public void Update(IRFuncDef func) throws OPTError {
        for (var block : func.getBlockstmts()) {
            HashMap<IRVariable, IRPhi> newPhiList = new HashMap<>();
            ArrayList<IRInst> newInsts = new ArrayList<>();
            for (var phi : block.getPhiList().values()) {
                if (Live.contains(phi)) {
                    newPhiList.put(phi.getDef(), phi);
                }
            }
            for (var inst : block.getInsts()) {
                if (Live.contains(inst)) {
                    newInsts.add(inst);
                }
            }
            block.setPhiList(newPhiList);
            block.setInsts(newInsts);
            if (!Live.contains(block.getReturnInst())) {
                var iter = block.getRidom();
                if (iter == null) {
                    continue;
                }
                while (!LiveBlock.contains(iter) && iter != iter.getRidom()) {
                    iter = iter.getRidom();
                }
                block.setReturnInst(new IRBranch(++InstCounter.InstCounter, iter.getLabelName()));
                block.getSuccessors().clear();
                block.getSuccessors().add(iter);
                iter.getPredecessors().add(block);
            }
        }
    }

    public void Work() throws OPTError {
        while (!WorkList.isEmpty()) {
            var x = WorkList.iterator().next();
            if (Live.contains(x)) {
                throw new OPTError("Live error");
            }
            Live.add(x);
            LiveBlock.add(inst2block.get(x));
            WorkList.remove(x);
            if (x instanceof IRPhi) {
                for (var label : ((IRPhi) x).getLabels()) {
                    var parent = name2block.get(label);
                    if (!Live.contains(parent.getReturnInst())) {
                        WorkList.add(parent.getReturnInst());
                        // LiveBlock.add(parent);
                    }
                }
            }
            var curblock = inst2block.get(x);
            for (var pred : curblock.getRDomFrontier()) {
                if (!Live.contains(pred.getReturnInst())) {
                    if(pred.getReturnInst() instanceof IRBranch)
                    {
                        if(((IRBranch)pred.getReturnInst()).isJump())
                        {
                            throw new OPTError("Jump error");
                        }
                    }
                    else if(pred.getReturnInst() instanceof IRRet)
                    {
                        throw new OPTError("Jump error");
                    }
                    WorkList.add(pred.getReturnInst());
                }
            }
            for (var use : x.getUses()) {
                var def = var2def.get(use);
                if (def != null && !Live.contains(def)) {
                    WorkList.add(def);
                }
            }
        }
    }

    private boolean SideEffect(IRInst inst) {
        if (inst instanceof IRCall) {
            return true;
        } else if (inst instanceof IRRet) {
            return true;
        } else if (inst instanceof IRStore) {
            return true;
        } else {
            return false;
        }
    }
}
