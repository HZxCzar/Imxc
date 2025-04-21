package Compiler.Src.OPT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.HashMap;

import Compiler.Src.IR.Node.Stmt.IRBlock;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.IR.Node.IRRoot;
import Compiler.Src.IR.Node.Def.IRFuncDef;
import Compiler.Src.IR.Node.Inst.IRBranch;
import Compiler.Src.IR.Node.Inst.IRRet;

public class CFGBuilder {
    private HashMap<IRLabel, IRBlock> label2Block;
    // private HashSet<IRBlock> visited = new HashSet<>();
    // private IRFuncDef currentFunc;

    public void visit(IRRoot root) {
        for (var func : root.getFuncs()) {
            init(func);
            visit(func);
        }
    }

    public void init(IRFuncDef func) {
        for (var block : func.getBlockstmts()) {
            block.getPredecessors().clear();
            block.getSuccessors().clear();
        }
    }

    public void visit(IRFuncDef funcDef) {
        var blocks = funcDef.getBlockstmts();
        // currentFunc = funcDef;
        label2Block = new HashMap<IRLabel, IRBlock>();
        // visited = new HashSet<IRBlock>();
        for (var block : blocks) {
            label2Block.put(block.getLabelName(), block);
        }
        for (var block : blocks) {
            visit(block);
        }
        var deadBlock = new HashSet<IRBlock>();
        for (var block : blocks) {
            if (block.getLabelName().getLabel().equals("entry")) {
                continue;
            }
            if (block.getPredecessors().size() == 0) {
                deadBlock.add(block);
                for (var next : block.getSuccessors()) {
                    next.getPredecessors().remove(block);
                }
            }
        }
        for (var dead : deadBlock) {
            blocks.remove(dead);
        }
        funcDef.setBlockstmts(blocks);
        if(funcDef.getBlockstmts().size() == 0){
            throw new OPTError("empty function");
        }
        // CalcRpo(funcDef.getBlockstmts().get(0));
    }

    public void visit(IRBlock block) {
        var returnInst = block.getReturnInst();
        if (returnInst instanceof IRBranch) {
            if (((IRBranch) returnInst).isJump()) {
                var nextBlock = label2Block.get(((IRBranch) returnInst).getTrueLabel());
                block.addSuccessor(nextBlock);
                nextBlock.addPredecessor(block);
            } else {
                var trueBlock = label2Block.get(((IRBranch) returnInst).getTrueLabel());
                var falseBlock = label2Block.get(((IRBranch) returnInst).getFalseLabel());
                block.addSuccessor(trueBlock);
                block.addSuccessor(falseBlock);
                trueBlock.addPredecessor(block);
                falseBlock.addPredecessor(block);
            }
        } else if (returnInst instanceof IRRet) {
            return;
        } else {
            throw new OPTError("returnInst is not IRBranch");
        }
    }

    // public void CalcRpo(IRBlock block) {
    // visited.add(block);
    // for (var succ : block.getSuccessors()) {
    // if (!visited.contains(succ)) {
    // CalcRpo(succ);
    // }
    // }
    // currentFunc.getBlock2Order().put(block, currentFunc.getBlock2Order().size());
    // currentFunc.getOrder2Block().add(0,block);
    // }
}
