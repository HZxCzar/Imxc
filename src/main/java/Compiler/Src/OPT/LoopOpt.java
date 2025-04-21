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
import java.util.Objects;

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

// class PtrUnit {
//     String type;
//     IREntity ptr;
//     ArrayList<IREntity> infolist;

//     public PtrUnit() {
//         infolist = new ArrayList<>();
//     }

//     public PtrUnit(IRGetelementptr inst) {
//         this.type = inst.getType();
//         this.ptr = inst.getPtr();
//         this.infolist = inst.getInfolist();
//     }

//     @Override
//     public boolean equals(Object obj) {
//         if (obj instanceof PtrUnit) {
//             boolean flag = true;
//             if (!((PtrUnit) obj).type.equals(type))
//                 flag = false;
//             if (!((PtrUnit) obj).ptr.equals(ptr))
//                 flag = false;
//             if (((PtrUnit) obj).infolist.size() != infolist.size())
//                 flag = false;
//             for (int i = 0; i < infolist.size(); ++i) {
//                 if (!((PtrUnit) obj).infolist.get(i).equals(infolist.get(i))) {
//                     flag = false;
//                     break;
//                 }
//             }
//             return flag;
//         }
//         return false;
//     }

//     @Override
//     public int hashCode() {
//         int result = Objects.hash(type);
//         result = 31 * result + Objects.hash(ptr.getValue());
//         for (int i = 0; i < infolist.size(); ++i) {
//             result = 31 * result + Objects.hash(infolist.get(i).getValue());
//         }
//         // result = 31 * result + infolist.hashCode();
//         return result;
//     }
// }

class Loop {
    Loop parent;
    int depth;
    IRBlock head;
    boolean call;
    ArrayList<Loop> children;
    HashSet<IRBlock> selfblocks;
    HashSet<IRVariable> StoreUse;
    HashSet<IRVariable> defs;
    // HashSet<PtrUnit> ptrs;

    public Loop(int depth) {
        this.parent = null;
        this.depth = depth;
        call = false;
        this.children = new ArrayList<>();
        this.selfblocks = new HashSet<>();
        this.StoreUse = new HashSet<>();
        this.defs = new HashSet<>();
        // this.ptrs = new HashSet<>();
    }
}

public class LoopOpt {//one time, cus we don't collect outside getelementptr, some load can't be moved out
    Loop base;
    HashMap<IRBlock, Loop> block2loop;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    public void work_on_func(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        base = new Loop(0);
        block2loop = new HashMap<>();
        LoopConstruct(func);
        work(func);
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
        // HashSet<IRBlock> visited = new HashSet<>();
        // HashSet<IRBlock> WorkList = new HashSet<>();
        // for(var block:func.getBlockstmts())
        // {
        // WorkList.add(block);
        // }
        while (base.children.size() != 0) {
            Loop cur = base.children.get(0);
            while (cur.children.size() != 0) {
                cur = cur.children.get(0);
            }
            boolean expand = true;
            HashSet<IRVariable> Changed = new HashSet<>();
            // HashSet<IRVariable> LoadFix = new HashSet<>();
            // HashSet<IRGetelementptr> getinst=new HashSet<>();
            Changed.addAll(cur.defs);
            // HashSet<PtrUnit> PtrUse = new HashSet<>();
            // HashSet<IRVariable> StoreUse=new HashSet<>();
            // HashMap<IRVariable,IRInst> Var2Def=new HashMap<>();
            // HashSet<IRInst> headerInst=new HashSet<>();
            ArrayList<IRInst> headerInstList = new ArrayList<>();
            for (var block : cur.selfblocks) {
                for (var phi : block.getPhiList().values()) {
                    Changed.add(phi.getDef());
                    cur.defs.add(phi.getDef());
                }
                for (var inst : block.getInsts()) {
                    if (inst.getDef() != null) {
                        Changed.add(inst.getDef());
                        cur.defs.add(inst.getDef());
                    }
                    if (inst instanceof IRStore) {
                        cur.StoreUse.add(((IRStore) inst).getDest());
                    }
                    if (inst instanceof IRCall) {
                        cur.call = true;
                    }
                    // if(inst instanceof IRGetelementptr)
                    // {
                    // getinst.add((IRGetelementptr)inst);
                    // }
                }
            }
            // for (var block : cur.selfblocks) {
            // for (var inst : block.getInsts()) {
            // if (inst instanceof IRLoad) {
            // if (cur.StoreUse.contains(((IRLoad) inst).getUses().get(0))) {
            // if (!LoadFix.contains(((IRLoad) inst).getUses().get(0))) {
            // LoadFix.add(((IRLoad) inst).getUses().get(0));
            // }
            // continue;
            // } else if (cur.call && ((IRLoad) inst).getUses().get(0).isGlobal()) {
            // if (!LoadFix.contains(((IRLoad) inst).getUses().get(0))) {
            // LoadFix.add(((IRLoad) inst).getUses().get(0));
            // }
            // continue;
            // }
            // }
            // }
            // }
            // for(var inst:getinst)
            // {
            // if (LoadFix.contains(((IRGetelementptr) inst).getDef())) {
            // var ptr = new PtrUnit((IRGetelementptr) inst);
            // if (!cur.ptrs.contains(ptr)) {
            // cur.ptrs.add(ptr);
            // }
            // // cur.StoreUse.add(inst.getDef());
            // // continue;
            // }
            // }
            // for(var inst:getinst)
            // {
            // var ptrUnit=new PtrUnit(inst);
            // if(cur.ptrs.contains(ptrUnit))
            // {
            // cur.StoreUse.add(inst.getDef());
            // }
            // }
            while (expand) {
                expand = false;
                for (var block : cur.selfblocks) {
                    for (int i = 0; i < block.getInsts().size(); ++i) {
                        var inst = block.getInsts().get(i);
                        if (inst.getDef() == null || !Changed.contains(inst.getDef()) || inst instanceof IRCall)
                            continue;
                        boolean flag = true;
                        if (inst instanceof IRLoad) {
                            if (cur.StoreUse.contains(((IRLoad) inst).getUses().get(0))) {
                                flag = false;
                                continue;
                            } else if (cur.call && ((IRLoad) inst).getUses().get(0).isGlobal()) {
                                flag = false;
                                continue;
                            }
                        }
                        // if (inst instanceof IRGetelementptr) {
                        // // continue;
                        // if(cur.StoreUse.contains(inst.getDef()))
                        // {
                        // continue;
                        // }
                        // }
                        for (var use : inst.getUses()) {
                            if (Changed.contains(use)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            if (inst instanceof IRGetelementptr) {
                                // Changed.remove(inst.getDef());
                                // headerInst.add(inst);
                                headerInstList.add(inst);
                                block.getInsts().remove(i);
                                i--;
                                expand = true;
                            } else {
                                Changed.remove(inst.getDef());
                                // headerInst.add(inst);
                                headerInstList.add(inst);
                                block.getInsts().remove(i);
                                i--;
                                expand = true;
                            }
                        }
                    }
                }
            }
            IRBlock header = cur.head;
            if (header == null) {
                if (cur != base) {
                    throw new OPTError("Loop head error");
                }
                break;
            }
            for (var inst : headerInstList) {
                header.addInsts(inst);
            }
            if (cur.call) {
                cur.parent.call = true;
            }
            cur.parent.StoreUse.addAll(cur.StoreUse);
            cur.parent.defs.addAll(cur.defs);
            // cur.parent.ptrs.addAll(cur.ptrs);
            cur.parent.children.remove(cur);
        }
    }

    public void LoopConstruct(IRFuncDef func) {
        Loop cur = null;
        HashSet<IRBlock> visited = new HashSet<>();
        HashSet<IRBlock> WorkList = new HashSet<>();
        block2loop.put(func.getBlockstmts().get(0), base);
        base.selfblocks.add(func.getBlockstmts().get(0));
        base.head = null;
        WorkList.add(func.getBlockstmts().get(0));
        while (!WorkList.isEmpty()) {
            IRBlock block = WorkList.iterator().next();
            WorkList.remove(block);
            cur = block2loop.get(block);
            visited.add(block);
            for (var succ : block.getSuccessors()) {
                if (visited.contains(succ))
                    continue;
                WorkList.add(succ);
                var depth = succ.getLoopDepth();
                if (depth == cur.depth + 1) {
                    if (block.getSuccessors().size() > 1) {
                        throw new OPTError("Loop succ num error");
                    }
                    if (succ.getPredecessors().size() > 1) {
                        int count = 0;
                        for (var pred : succ.getPredecessors()) {
                            if (pred.getLoopDepth() < depth) {
                                count++;
                            }
                        }
                        if (count != 1) {
                            throw new OPTError("Loop pred num error");
                            // int a=1;
                        }
                        // for(var pred:succ.getPredecessors())
                        // {
                        // if(pred.getLoopDepth()<cur.depth)
                        // {
                        // count++;
                        // }
                        // }
                    }
                    Loop sucloop = new Loop(depth);
                    sucloop.parent = cur;
                    cur.children.add(sucloop);
                    block2loop.put(succ, sucloop);
                    sucloop.selfblocks.add(succ);
                    sucloop.head = block;
                } else if (depth == cur.depth) {
                    cur.selfblocks.add(succ);
                    block2loop.put(succ, cur);
                } else if (depth == cur.depth - 1) {
                    Loop parloop = cur.parent;
                    parloop.selfblocks.add(succ);
                    block2loop.put(succ, parloop);
                } else {
                    throw new OPTError("Loop depth error");
                }
            }
        }
    }
}
