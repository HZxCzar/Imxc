package Compiler.Src.OPT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
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

class RHS {
    private String op;
    private IREntity a, b;

    public RHS(String op, IREntity a, IREntity b) {
        this.op = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RHS) {
            return this.hashCode() == ((RHS) obj).hashCode();
        }
        return false;
    }
}

public class CSE {
    private Queue<IRBlock> q;
    private HashMap<IRVariable, HashSet<IRInst>> Var2Use;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work(func));
    }

    // public boolean jud(IRFuncDef func)
    // {
    //     if(func.getBlockstmts().size()>4000)
    //     {
    //         return true;
    //     }
    //     return false;
    // }

    public void work(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        new DomTreeBuilder().visit(func);
        Var2Use = new HashMap<>();
        Collect(func);
        q = new LinkedList<>();
        HashMap<RHS, ArrayList<IRVariable>> destMap = new HashMap<>();
        HashMap<RHS, ArrayList<IRBlock>> blockMap = new HashMap<>();
        for (int round = 0; round < 2; ++round) {
            q.add(func.getBlockstmts().get(0));
            while (!q.isEmpty()) {
                var block = q.poll();
                for (int ind = 0; ind < block.getInsts().size(); ++ind) {
                    var inst = block.getInsts().get(ind);
                    RHS rhs1 = null, rhs2 = null;
                    if (inst instanceof IRArith) {
                        rhs1 = new RHS(((IRArith) inst).getOp(), ((IRArith) inst).getLhs(), ((IRArith) inst).getRhs());
                        var op = ((IRArith) inst).getOp();
                        switch (op) {
                            case "add":
                                rhs2 = new RHS(op, ((IRArith) inst).getRhs(),
                                        ((IRArith) inst).getLhs());
                                break;
                            case "mul":
                                rhs2 = new RHS(op, ((IRArith) inst).getRhs(),
                                        ((IRArith) inst).getLhs());
                                break;
                            case "and":
                                rhs2 = new RHS(op, ((IRArith) inst).getRhs(),
                                        ((IRArith) inst).getLhs());
                                break;
                            case "or":
                                rhs2 = new RHS(op, ((IRArith) inst).getRhs(),
                                        ((IRArith) inst).getLhs());
                                break;
                            case "xor":
                                rhs2 = new RHS(op, ((IRArith) inst).getRhs(),
                                        ((IRArith) inst).getLhs());
                                break;

                            default:
                                break;
                        }
                    } else if (inst instanceof IRIcmp) {
                        rhs1 = new RHS(((IRIcmp) inst).getCond(), ((IRIcmp) inst).getLhs(), ((IRIcmp) inst).getRhs());
                        var op = ((IRIcmp) inst).getCond();
                        switch (op) {
                            case "eq":
                                rhs2 = new RHS(op, ((IRIcmp) inst).getRhs(),
                                        ((IRIcmp) inst).getLhs());
                                break;
                            case "neq":
                                rhs2 = new RHS(op, ((IRIcmp) inst).getRhs(),
                                        ((IRIcmp) inst).getLhs());
                                break;
                            default:
                                break;
                        }
                    } else if (inst instanceof IRGetelementptr) {
                        rhs1 = new RHS("IRGetelementptr", ((IRGetelementptr) inst).getPtr(), ((IRGetelementptr) inst)
                                .getInfolist().get(((IRGetelementptr) inst).getInfolist().size() - 1));
                    } else {
                        continue;
                    }
                    boolean flag = false;
                    if (destMap.get(rhs1) != null) {
                        var destList = destMap.get(rhs1);
                        var blockList = blockMap.get(rhs1);
                        for (int i = 0; i < destList.size(); ++i) {
                            if (isDomChildren(block, blockList.get(i)) && !inst.getDef().equals(destList.get(i))) {
                                flag = true;
                                replaceVariable(inst.getDef(), destList.get(i));
                                block.getInsts().remove(ind);
                                --ind;
                                for (var use : inst.getUses()) {
                                    Var2Use.get(use).remove(inst);
                                }
                                break;
                            }
                        }
                    }
                    if (!flag && rhs2 != null && destMap.get(rhs2) != null) {
                        var destList = destMap.get(rhs2);
                        var blockList = blockMap.get(rhs2);
                        for (int i = 0; i < destList.size(); ++i) {
                            if (isDomChildren(block, blockList.get(i)) && !inst.getDef().equals(destList.get(i))) {
                                flag = true;
                                replaceVariable(inst.getDef(), destList.get(i));
                                block.getInsts().remove(ind);
                                --ind;
                                for (var use : inst.getUses()) {
                                    Var2Use.get(use).remove(inst);
                                }
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        if (destMap.get(rhs1) != null) {
                            if (!destMap.get(rhs1).contains(inst.getDef())) {
                                destMap.get(rhs1).add(inst.getDef());
                                blockMap.get(rhs1).add(block);
                            }
                        } else {
                            ArrayList<IRVariable> destList = new ArrayList<IRVariable>();
                            destList.add(inst.getDef());
                            destMap.put(rhs1, destList);
                            ArrayList<IRBlock> blockList = new ArrayList<IRBlock>();
                            blockList.add(block);
                            blockMap.put(rhs1, blockList);
                        }
                        if (rhs2 != null) {
                            if (destMap.get(rhs2) != null) {
                                if (!destMap.get(rhs2).contains(inst.getDef())) {
                                    destMap.get(rhs2).add(inst.getDef());
                                    blockMap.get(rhs2).add(block);
                                }
                            } else {
                                ArrayList<IRVariable> destList = new ArrayList<IRVariable>();
                                destList.add(inst.getDef());
                                destMap.put(rhs2, destList);
                                ArrayList<IRBlock> blockList = new ArrayList<IRBlock>();
                                blockList.add(block);
                                blockMap.put(rhs2, blockList);
                            }
                        }
                    }
                }
                for (var domchild : block.getDomChildren()) {
                    q.add(domchild);
                }
            }
        }
    }

    public boolean isDomChildren(IRBlock block, IRBlock parent) {
        var iter = block;
        while (!iter.equals(parent) && !iter.getIdom().equals(iter)) {
            iter = iter.getIdom();
        }
        if (iter.equals(parent)) {
            return true;
        }
        return false;
    }

    public void Collect(IRFuncDef func) {
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
                Var2Use.get(use).add(block.getReturnInst());
            }
        }
    }

    public void replaceVariable(IRVariable origin, IRVariable rep) {
        for (var inst : Var2Use.get(origin)) {
            inst.replaceUse(origin, rep);
        }
    }
}