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

public class LoadStoreOpt {
    HashMap<String, HashSet<IRInst>> GlobalVar;
    HashSet<IRFuncDef> forbid;
    HashSet<IRBlock> RetBlock;
    HashMap<String, IRType> Var2Type;
    // HashSet<Pair<IRBlock, IRCall>> CallInsts;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        BUildMap(root);
        BuildCallMap(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    // public boolean jud(IRFuncDef func)
    // {
    //     if(func.getBlockstmts().size()>4000)
    //     {
    //         return true;
    //     }
    //     return false;
    // }

    public void BuildCallMap(IRRoot root) {
        forbid = new HashSet<>();
        for (var func : root.getFuncs()) {
            for (var block : func.getBlockstmts()) {
                for (var inst : block.getInsts()) {
                    if (inst instanceof IRCall) {
                        if (!((IRCall) inst).getFuncName().equals("main.global.init")) {
                            forbid.add(func);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void BUildMap(IRRoot root) {
        Var2Type = new HashMap<>();
        for (var glob : root.getDefs()) {
            Var2Type.put(glob.getVars().getValue(), glob.getVars().getType());
        }
    }

    public void work_on_func(IRFuncDef func) {
        // if(jud(func))
        // {
        //     return;
        // }
        if (func.getName().equals("main.global.init"))
            return;
        else if (forbid.contains(func)) {
            return;
        }
        Collect(func);
        work(func);
    }

    public void Collect(IRFuncDef func) {
        GlobalVar = new HashMap<>();
        RetBlock = new HashSet<>();
        // CallInsts = new HashSet<>();
        for (var block : func.getBlockstmts()) {
            for (var inst : block.getInsts()) {
                for (var use : inst.getUses()) {
                    if (use.isGlobal() && !use.isStr()) {
                        if (!GlobalVar.containsKey(use.getValue())) {
                            GlobalVar.put(use.getValue(), new HashSet<IRInst>());
                        }
                        GlobalVar.get(use.getValue()).add(inst);
                    }
                }
            }
            if (block.getReturnInst() instanceof IRRet) {
                RetBlock.add(block);
            }
        }
    }

    public void work(IRFuncDef func) {
        var entry = func.getBlockstmts().get(0);
        for (var pair : GlobalVar.entrySet()) {
            if (pair.getValue().size() < 3) {
                continue;
            }
            var start = func.getBlockstmts().get(1);
            String glob = pair.getKey();
            IRVariable globunit = new IRVariable(GlobalScope.irPtrType, glob);
            IRVariable local = new IRVariable(GlobalScope.irPtrType,
                    "%" + glob.substring(1) + ".local." + func.getName());
            IRVariable tmp = new IRVariable(Var2Type.get(glob),
                    "%" + glob.substring(1) + ".local.tmp." + func.getName());
            entry.addInsts(new IRAlloca(++InstCounter.InstCounter, local, Var2Type.get(glob)));
            start.getInsts().add(0, new IRStore(++InstCounter.InstCounter, local, tmp));
            start.getInsts().add(0, new IRLoad(++InstCounter.InstCounter, tmp, globunit));
            for (var inst : pair.getValue()) {
                inst.replaceUse(globunit, local);
            }
            for (var block : RetBlock) {
                // if (block.getInsts().size() == 0) {
                //     continue;
                // }
                IRVariable tmpret = new IRVariable(Var2Type.get(glob), "%" + glob.substring(1)
                        + ".local.tmp.ret." + func.getName() + "." + block.getLabelName().getLabel());
                block.getInsts().add(new IRLoad(++InstCounter.InstCounter, tmpret, local));
                block.getInsts().add(new IRStore(++InstCounter.InstCounter, globunit, tmpret));
            }
        }
    }
}
