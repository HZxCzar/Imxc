package Compiler.Src.OPT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.HashMap;

import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.IRRoot;
import Compiler.Src.IR.Node.Def.IRFuncDef;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.IRBlock;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

public class DomTreeBuilder {
    private IRFuncDef currentFunc;
    private ArrayList<IRBlock> PostOrder = new ArrayList<IRBlock>();
    private HashSet<IRBlock> visited = new HashSet<IRBlock>();

    private Stack<IRBlock> WorkStack = new Stack<IRBlock>();

    public void visit(IRFuncDef func) {
        currentFunc = func;
        PostOrder = new ArrayList<IRBlock>();
        visited = new HashSet<IRBlock>();
        init(func);
        buildDom(func);
        return;
    }

    public void init(IRFuncDef func) {
        for (var block : func.getBlockstmts()) {
            block.getDomChildren().clear();
            block.getDomFrontier().clear();
            block.setIdom(null);
        }
    }

    public void CalcRpo(IRBlock block) {
        visited.add(block);
        WorkStack.push(block);
        Boolean run = true;
        while (!WorkStack.empty()) {
            run = false;
            var cur = WorkStack.peek();
            for (var succ : cur.getSuccessors()) {
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
                currentFunc.getOrder2Block().add(0, postblock);
            }
            // PostOrder.add(cur);
            // currentFunc.getOrder2Block().add(0,cur);
        }
        // for (var succ : block.getSuccessors()) {
        // if (!visited.contains(succ)) {
        // CalcRpo(succ);
        // }
        // }
        // currentFunc.getBlock2Order().put(block, currentFunc.getBlock2Order().size());
        PostOrder.add(block);
        currentFunc.getOrder2Block().add(0, block);
    }

    public void buildDom(IRFuncDef func) {
        var entryBlock = func.getBlockstmts().get(0);
        WorkStack = new Stack<IRBlock>();
        CalcRpo(entryBlock);
        entryBlock.setIdom(entryBlock);
        // build DomTree
        boolean run = true;
        while (run) {
            run = false;
            for (int i = PostOrder.size() - 1; i >= 0; --i) {
                var block = PostOrder.get(i);
                if (block == entryBlock) {
                    continue;
                }
                if (calcIdom(block)) {
                    run = true;
                }
            }

            // for (var block : func.getOrder2Block()) {
            //     if (block == entryBlock) {
            //         continue;
            //     }
            //     if (calcIdom(block)) {
            //         run = true;
            //     }
            // }
        }

        // build DomFrontier
        for (int i = PostOrder.size() - 1; i >= 0; --i) {
            var block = PostOrder.get(i);
            if (block.getIdom() != block) {
                block.getIdom().getDomChildren().add(block);
            }
            calcDF(block);
        }
        // for (var block : func.getOrder2Block()) {
        //     if (block.getIdom() != block) {
        //         block.getIdom().getDomChildren().add(block);
        //     }
        //     calcDF(block);
        // }
        return;
    }

    public boolean calcIdom(IRBlock block) {
        IRBlock newIdom = null;
        for (var pred : block.getPredecessors()) {
            if (newIdom == null && pred.getIdom() != null) {// ???
                newIdom = pred;
            } else if (pred.getIdom() != null) {
                newIdom = intersect(pred, newIdom);
            }
        }
        if (block.getIdom() != newIdom) {
            block.setIdom(newIdom);
            return true;
        }
        return false;
    }

    public IRBlock intersect(IRBlock b1, IRBlock b2) { // LCA
        while (b1 != b2) {
            while (PostOrder.indexOf(b1) < PostOrder.indexOf(b2)) {
                b1 = b1.getIdom();
            }
            while (PostOrder.indexOf(b1) > PostOrder.indexOf(b2)) {
                b2 = b2.getIdom();
            }
            // while (currentFunc.getBlock2Order().get(b1) <
            // currentFunc.getBlock2Order().get(b2)) {
            // b1 = b1.getIdom();
            // }
            // while (currentFunc.getBlock2Order().get(b1) >
            // currentFunc.getBlock2Order().get(b2)) {
            // b2 = b2.getIdom();
            // }
        }
        return b1;
    }

    public void calcDF(IRBlock block) {
        for (var pred : block.getPredecessors()) {
            var runner = pred;
            while (runner != block.getIdom()) // entry?
            {
                runner.getDomFrontier().add(block);
                runner = runner.getIdom();
            }
        }
    }

}
