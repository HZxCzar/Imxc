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
import lombok.val;

public class PhiRep {
    HashMap<IRVariable, IREntity> varMap;
    HashMap<IRVariable, HashSet<IRInst>> Var2Use;
    boolean change;

    public void visit(IRRoot root) {
        new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work_on_func(func));
    }

    public void work_on_func(IRFuncDef func) {
        change = true;
        while (change) {
            change = false;
            varMap = new HashMap<>();
            Var2Use = new HashMap<>();
            for (var block : func.getBlockstmts()) {
                for (var phiInst : block.getPhiList().values()) {
                    for (int i = 0; i < phiInst.getVals().size(); ++i) {
                        var label = phiInst.getLabels().get(i);
                        var valid = false;
                        for (var pred : block.getPredecessors()) {
                            if (pred.getLabelName().equals(label)) {
                                valid = true;
                            }
                        }
                        if (!valid) {
                            change = true;
                            phiInst.getVals().remove(i);
                            phiInst.getLabels().remove(i);
                            --i;
                        }
                    }
                }
            }
            init(func);
            for (var origin : varMap.keySet()) {
                var rep = varMap.get(origin);
                while (rep instanceof IRVariable && varMap.containsKey(rep)) {
                    rep = varMap.get(rep);
                }
                varMap.put(origin, rep);
            }
            for (var origin : varMap.keySet()) {
                if(!Var2Use.containsKey(origin))
                {
                    continue;
                }
                for (var inst : Var2Use.get(origin)) {
                    change = true;
                    inst.replaceUse(origin, varMap.get(origin));
                }
            }
        }
    }

    public void init(IRFuncDef func) {
        // for (var block : func.getBlockstmts()) {
        //     var phiList = new HashMap<IRVariable, IRPhi>();
        //     for (var phi : block.getPhiList().values()) {
        //         if (phi.getVals().size() == 1) {
        //             change = true;
        //             varMap.put(phi.getDef(), phi.getVals().get(0));
        //         } else {
        //             phiList.put(phi.getDef(), phi);
        //         }
        //     }
        //     block.setPhiList(phiList);
        // }
        for (var arg : func.getParams()) {
            Var2Use.put(arg, new HashSet<IRInst>());
        }
        for (var block : func.getBlockstmts()) {
            if(block.getLabelName().getLabel().equals("inline.3.inline.11.inline.15"))
            {
                int a=1;
            }
            var phiList = new HashMap<IRVariable, IRPhi>();
            for (var phiInst : block.getPhiList().values()) {
                if (phiInst.getVals().size() == 1) {
                    change = true;
                    varMap.put(phiInst.getDef(), phiInst.getVals().get(0));
                } else {
                    if(phiInst.getDef().getValue().equals("%call.5.inline.11.inline.15"))
                    {
                        int a=1;
                    }
                    phiList.put(phiInst.getDef(), phiInst);
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
            }
            block.setPhiList(phiList);
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
}
