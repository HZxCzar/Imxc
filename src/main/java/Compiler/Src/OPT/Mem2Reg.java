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

public class Mem2Reg {

    private IRFuncDef currentFunc;

    // insertPhi
    private HashMap<IRVariable, IRType> Var2Type;
    private HashMap<IRVariable, ArrayList<IRBlock>> Var2Block;

    private ArrayList<IRBlock> PostOrder = new ArrayList<IRBlock>();
    private HashSet<IRBlock> visited = new HashSet<IRBlock>();

    private Stack<IRBlock> WorkStack = new Stack<IRBlock>();

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        for (var func : root.getFuncs()) {
            visit(func);
        }
    }

    public void visit(IRFuncDef func) {
        currentFunc = func;
        PostOrder = new ArrayList<IRBlock>();
        visited = new HashSet<IRBlock>();
        buildDom(func);
        // new DomTreeBuilder().visit(func);
        insertPhi(func);
        rename(func);
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
            // if (block == entryBlock) {
            // continue;
            // }
            // if (calcIdom(block)) {
            // run = true;
            // }
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
        for(int i=0;i<func.getBlockstmts().size();++i)
        {
            var block = func.getBlockstmts().get(i);
            if(!PostOrder.contains(block))
            {
                func.getBlockstmts().remove(i);
                --i;
                // throw new OPTError("block not in postorder");
            }
        }
        // for (var block : func.getOrder2Block()) {
        // if (block.getIdom() != block) {
        // block.getIdom().getDomChildren().add(block);
        // }
        // calcDF(block);
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

    public void insertPhi(IRFuncDef func) {
        AllocaCollector(func);
        for (var var : Var2Block.keySet()) {
            // BFS
            var WorkQue = new LinkedList<IRBlock>();
            var util = new HashSet<IRBlock>();
            for (var block : Var2Block.get(var)) {
                WorkQue.add(block);
                util.add(block);
            }
            while (!WorkQue.isEmpty()) {
                var centerblock = WorkQue.poll();
                for (var block : centerblock.getDomFrontier()) {
                    if (block.getPhiList().containsKey(var)) {
                        continue;
                    }
                    var PhiDest = new IRVariable(Var2Type.get(var),
                            var.getValue() + ".PhiBlock." + PostOrder.indexOf(block));
                    var PhiInst = new IRPhi(++InstCounter.InstCounter, PhiDest, PhiDest.getType(),
                            new ArrayList<IREntity>(),
                            new ArrayList<IRLabel>());
                    block.getPhiList().put(var, PhiInst);
                    if (!util.contains(block)) {
                        util.add(block);
                        WorkQue.add(block);
                    }
                }
            }
        }
    }

    public void AllocaCollector(IRFuncDef func) {
        Var2Type = new HashMap<>();
        Var2Block = new HashMap<>();
        // var entryBlock = func.getBlockstmts().get(0);
        // for (var inst : entryBlock.getInsts()) {
        // if (inst instanceof IRAlloca) {
        // Var2Type.put(((IRAlloca) inst).getDest(), ((IRAlloca) inst).getType());
        // Var2Block.put(((IRAlloca) inst).getDest(), new ArrayList<IRBlock>());
        // }
        // }
        for (var block : func.getBlockstmts()) {
            for (var inst : block.getInsts()) {
                if (inst instanceof IRAlloca) {
                    Var2Type.put(((IRAlloca) inst).getDest(), ((IRAlloca) inst).getType());
                    Var2Block.put(((IRAlloca) inst).getDest(), new ArrayList<IRBlock>());
                }
                if (inst instanceof IRStore) {
                    var dest = (IRVariable) ((IRStore) inst).getDest();
                    if (Var2Type.get(dest) == null) {
                        continue;
                    }
                    Var2Block.get(dest).add(block);
                } else if (inst instanceof IRCall && ((IRCall) inst).getFuncName().equals("__string.copy")) {
                    var dest = (IRVariable) ((IRCall) inst).getArgs().get(0);
                    if (Var2Type.get(dest) == null) {
                        continue;
                    }
                    Var2Block.get(dest).add(block);
                }
            }
        }
    }

    public void rename(IRFuncDef func) {
        var var2entity = new HashMap<IRVariable, IREntity>();
        var entryBlock = func.getBlockstmts().get(0);
        for (var block : func.getBlockstmts()) {
            if (block.getLabelName().getLabel().startsWith("entry")) {
                for (var inst : block.getInsts()) {
                    if (inst instanceof IRAlloca) {
                        var2entity.put(((IRAlloca) inst).getDest(), null);
                    }
                }
            }
        }
        // for (var inst : entryBlock.getInsts()) {
        // if (inst instanceof IRAlloca) {
        // var2entity.put(((IRAlloca) inst).getDest(), null);
        // }
        // }
        var reg2entity = new HashMap<IRVariable, IREntity>();
        // renameBlock(entryBlock, var2entity, reg2entity);
        BlockRename(entryBlock, var2entity, reg2entity, 0);
        var2entity = new HashMap<IRVariable, IREntity>();
        BlockRename(entryBlock, var2entity, reg2entity, 1);
    }

    public void BlockRename(IRBlock entryblock, HashMap<IRVariable, IREntity> entryvar2entity,
            HashMap<IRVariable, IREntity> entryreg2entity, Integer Round) {
        visited = new HashSet<IRBlock>();
        Stack<Pair<IRBlock, HashMap<IRVariable, IREntity>>> WorkStack = new Stack<>();
        WorkStack.push(new Pair<>(entryblock, entryvar2entity));
        visited.add(entryblock);
        var reg2entity = entryreg2entity;
        while (!WorkStack.empty()) {
            var unit = WorkStack.pop();
            var block = unit.a;
            var var2entity = unit.b;
            for (var phi : block.getPhiList().keySet()) {
                var2entity.put(phi, block.getPhiList().get(phi).getDest());
            }
            // var reg2entity = new HashMap<IRVariable, IREntity>();
            for (var pair : block.getPhiList().entrySet()) {
                if (!pair.getKey().equals(pair.getValue().getDest())) {
                    continue;
                }
                for (var val : pair.getValue().getVals()) {
                    if (val instanceof IRVariable && reg2entity.containsKey(val)) {
                        pair.getValue().replaceUse((IRVariable) val, reg2entity.get(val));
                    }
                }
            }
            var newInstList = new ArrayList<IRInst>();
            for (var inst : block.getInsts()) {
                if(inst instanceof IRCall)
                {
                    int a=1;
                }
                if (inst instanceof IRAlloca) {
                    continue;
                } else if (inst instanceof IRStore) {
                    if (((IRStore) inst).getDest().isParameter()) {
                        var entity = ((IRStore) inst).getSrc();
                        if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
                            entity = reg2entity.get(entity);
                        }
                        var2entity.put(((IRStore) inst).getDest(), entity);
                        continue;
                    }
                } else if (inst instanceof IRLoad) {
                    if (((IRLoad) inst).getPtr().isParameter()) {
                        var entity = var2entity.get(((IRLoad) inst).getPtr());
                        if (entity == null) {
                            entity = new IRLiteral(((IRLoad) inst).getDest().getType(), "0"); // alloca完第一次取
                        } else if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
                            entity = reg2entity.get(entity);
                        }
                        reg2entity.put(((IRLoad) inst).getDest(), entity);
                        continue;
                    }
                } else if (inst instanceof IRCall && ((IRCall) inst).getFuncName().equals("__string.copy")) {
                    if (((IRVariable) ((IRCall) inst).getArgs().get(0)).isParameter()) {
                        var entity = ((IRCall) inst).getArgs().get(1);
                        if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
                            entity = reg2entity.get(entity);
                        }
                        var2entity.put((IRVariable) ((IRCall) inst).getArgs().get(0), entity);
                        continue;
                    }
                }
                var uses = inst.getUses();
                for (var use : uses) {
                    if (reg2entity.containsKey(use)) {
                        inst.replaceUse(use, reg2entity.get(use));
                    }
                }
                newInstList.add(inst);
            }

            block.setInsts(newInstList);
            for (var use : block.getReturnInst().getUses()) {
                if (reg2entity.containsKey(use)) {
                    block.getReturnInst().replaceUse(use, reg2entity.get(use));
                }
            }
            if (Round == 0) {
                for (var succ : block.getSuccessors()) {
                    var phiList = succ.getPhiList();
                    for (var key : phiList.keySet()) {
                        if (key.equals(phiList.get(key).getDest())) {// TO FIX
                            continue;
                        }
                        var entity = var2entity.get(key);
                        if (entity == null) {
                            entity = new IRLiteral(phiList.get(key).getType(), "0");
                        }
                        phiList.get(key).getVals().add(entity);
                        phiList.get(key).getLabels().add(block.getLabelName());
                    }
                }
            }
            // Stack<Pair<IRBlock, HashMap<IRVariable, IREntity>>> TMP = new Stack<>();
            // for (var Domchild : block.getDomChildren()) {
            // var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
            // visited.add(Domchild);
            // TMP.push(new Pair<>(Domchild, var2entity2));
            // // WorkStack.push(new Pair<>(Domchild, var2entity2));
            // }
            // for (var Domchild : block.getDomChildren()) {
            // var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
            // TMP.push(new Pair<>(Domchild, var2entity2));
            // // WorkStack.push(new Pair<>(Domchild, var2entity2));
            // }
            // while (!TMP.empty()) {
            // WorkStack.push(TMP.pop());
            // }
            // for (int i = block.getDomChildren().size() - 1; i >= 0; --i) {
            // var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
            // var Domchild = block.getDomChildren().get(i);
            // visited.add(Domchild);
            // WorkStack.push(new Pair<>(Domchild, var2entity2));
            // }
            for (var Domchild : block.getDomChildren()) {
                var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
                visited.add(Domchild);
                WorkStack.push(new Pair<>(Domchild, var2entity2));
            }
            // for (var Domchild : block.getDomChildren()) {
            // var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
            // visited.add(Domchild);
            // WorkStack.push(new Pair<>(Domchild, var2entity2));
            // }
        }
    }
}

// public void renameBlock(IRBlock block, HashMap<IRVariable, IREntity> var2entity,
//             HashMap<IRVariable, IREntity> reg2entity) {
//         for (var phi : block.getPhiList().keySet()) {
//             var2entity.put(phi, block.getPhiList().get(phi).getDest());
//         }
//         // var reg2entity = new HashMap<IRVariable, IREntity>();
//         for (var pair : block.getPhiList().entrySet()) {
//             if (!pair.getKey().equals(pair.getValue().getDest())) {
//                 continue;
//             }
//             for (var val : pair.getValue().getVals()) {
//                 if (val instanceof IRVariable && reg2entity.containsKey(val)) {
//                     pair.getValue().replaceUse((IRVariable) val, reg2entity.get(val));
//                 }
//             }
//         }
//         var newInstList = new ArrayList<IRInst>();
//         for (var inst : block.getInsts()) {
//             if (inst instanceof IRAlloca) {
//                 continue;
//             } else if (inst instanceof IRStore) {
//                 if (((IRStore) inst).getDest().isParameter()) {
//                     var entity = ((IRStore) inst).getSrc();
//                     if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
//                         entity = reg2entity.get(entity);
//                     }
//                     var2entity.put(((IRStore) inst).getDest(), entity);
//                     continue;
//                 }
//             } else if (inst instanceof IRLoad) {
//                 if (((IRLoad) inst).getPtr().isParameter()) {
//                     var entity = var2entity.get(((IRLoad) inst).getPtr());
//                     if (entity == null) {
//                         entity = new IRLiteral(((IRLoad) inst).getDest().getType(), "0"); // alloca完第一次取
//                     } else if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
//                         entity = reg2entity.get(entity);
//                     }
//                     reg2entity.put(((IRLoad) inst).getDest(), entity);
//                     continue;
//                 }
//             } else if (inst instanceof IRCall && ((IRCall) inst).getFuncName().equals("__string.copy")) {
//                 if (((IRVariable) ((IRCall) inst).getArgs().get(0)).isParameter()) {
//                     var entity = ((IRCall) inst).getArgs().get(1);
//                     if (entity instanceof IRVariable && reg2entity.containsKey((IRVariable) entity)) {
//                         entity = reg2entity.get(entity);
//                     }
//                     var2entity.put((IRVariable) ((IRCall) inst).getArgs().get(0), entity);
//                     continue;
//                 }
//             }
//             var uses = inst.getUses();
//             for (var use : uses) {
//                 if (reg2entity.containsKey(use)) {
//                     inst.replaceUse(use, reg2entity.get(use));
//                 }
//             }
//             newInstList.add(inst);
//         }

//         block.setInsts(newInstList);
//         for (var use : block.getReturnInst().getUses()) {
//             if (reg2entity.containsKey(use)) {
//                 block.getReturnInst().replaceUse(use, reg2entity.get(use));
//             }
//         }
//         for (var succ : block.getSuccessors()) {
//             var phiList = succ.getPhiList();
//             for (var key : phiList.keySet()) {
//                 if (key.equals(phiList.get(key).getDest())) {// TO FIX
//                     continue;
//                 }
//                 var entity = var2entity.get(key);
//                 if (entity == null) {
//                     entity = new IRLiteral(phiList.get(key).getType(), "0");
//                 }
//                 phiList.get(key).getVals().add(entity);
//                 phiList.get(key).getLabels().add(block.getLabelName());
//             }
//         }
//         for (var Domchild : block.getDomChildren()) {
//             var var2entity2 = new HashMap<IRVariable, IREntity>(var2entity);
//             // var reg2entity2 = new HashMap<IRVariable, IREntity>(reg2entity);
//             renameBlock(Domchild, var2entity2, reg2entity);
//         }
//     }
