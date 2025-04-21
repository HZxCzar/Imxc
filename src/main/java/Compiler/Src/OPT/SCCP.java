package Compiler.Src.OPT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.ASM.Node.ASMNode;
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
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;
import lombok.Builder.Default;

public class SCCP implements IRVisitor<OPTError> {
    private HashSet<IRInst> EreaseWorkSet = new HashSet<>();
    private HashMap<IRVariable, IRInst> Var2Def = new HashMap<>();
    private HashMap<IRVariable, IRGlobalDef> Var2GDef = new HashMap<>();
    private HashMap<IRVariable, HashSet<IRInst>> Var2Use = new HashMap<>();
    private HashMap<IRInst, IRBlock> Inst2Block = new HashMap<>();
    private IRBlock currentBlock;

    // code move
    private HashMap<IRVariable, Pair<IRBlock, IRInst>> Var2Pair = new HashMap<>();

    // SCCP
    private HashMap<IRVariable, Pair<Integer, IREntity>> V;
    private HashSet<IRBlock> Excutable;

    @Override
    public OPTError visit(IRRoot root) throws BaseError {
        new CFGBuilder().visit(root);
        Collect(root);
        // Run(root);
        // new CFGBuilder().visit(root);
        root.getFuncs().forEach(func -> work(func));
        // Erease(root);
        // CodeMove(root);
        return new OPTError();
    }

    public void work(IRFuncDef func) {
        // if(jud(func))
        // {
        // return;
        // }
        V = new HashMap<>();
        Excutable = new HashSet<>();
        Var2Use = new HashMap<IRVariable, HashSet<IRInst>>();
        var WorkListV = new HashSet<IRVariable>();
        var WorkListB = new HashSet<IRBlock>();
        var label2Block = new HashMap<IRLabel, IRBlock>();
        for (var arg : func.getParams()) {
            Var2Use.put(arg, new HashSet<IRInst>());
            V.put(arg, new Pair<>(2, null));
            WorkListV.add(arg);
        }
        for (var block : func.getBlockstmts()) {
            label2Block.put(block.getLabelName(), block);
            for (var phiInst : block.getPhiList().values()) {
                V.put(phiInst.getDef(), new Pair<>(0, null));
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
                    V.put(inst.getDef(), new Pair<>(0, null));
                }
            }
            for (var use : block.getReturnInst().getUses()) {
                if (Var2Use.get(use) == null) {
                    Var2Use.put((IRVariable) use, new HashSet<IRInst>());
                }
                Var2Use.get(use).add(block.getReturnInst());
            }
        }
        for (var keys : Var2Use.keySet()) {
            if (V.get(keys) == null) {
                V.put(keys, new Pair<>(2, null));
            }
        }
        Excutable.add(func.getBlockstmts().get(0));
        WorkListB.add(func.getBlockstmts().get(0));
        while (!WorkListV.isEmpty() || !WorkListB.isEmpty()) {
            while (!WorkListB.isEmpty()) {
                var block = WorkListB.iterator().next();
                WorkListB.remove(block);
                for (var phiInst : block.getPhiList().values()) {
                    // 9
                    boolean fact9 = true;
                    IREntity entity = null;
                    for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                        var val = phiInst.getVals().get(i);
                        var label = phiInst.getLabels().get(i);
                        if (Excutable.contains(label2Block.get(label))) {
                            if (val instanceof IRVariable && V.get(val).a == 2) {
                                fact9 = false;
                                break;
                            } else if (val instanceof IRVariable && V.get(val).a == 0) {
                                continue;
                            }
                            if (entity == null) {
                                entity = val instanceof IRLiteral ? val : V.get(val).b;
                            } else if (!entity.equals(val instanceof IRLiteral ? val : V.get(val).b)) {
                                fact9 = false;
                                break;
                            }
                        }
                    }
                    fact9 = entity == null ? false : fact9;
                    if (fact9) {
                        if (V.get(phiInst.getDef()).a < 1) {
                            V.put(phiInst.getDef(), new Pair<>(1, entity));
                            WorkListV.add(phiInst.getDef());
                        }
                        continue;
                    }
                    // 6
                    boolean fact6 = false;
                    HashSet<IREntity> tmp = new HashSet<>();
                    for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                        var val = phiInst.getVals().get(i);
                        var label = phiInst.getLabels().get(i);
                        if (Excutable.contains(label2Block.get(label))
                                && (val instanceof IRLiteral || V.get(val).a == 1)) {
                            if (!tmp.isEmpty() && !tmp.contains(val)) {
                                fact6 = true;
                                break;
                            }
                            tmp.add(val);
                        }
                    }
                    if (fact6) {
                        if (V.get(phiInst.getDef()).a < 2) {
                            V.put(phiInst.getDef(), new Pair<>(2, null));
                            WorkListV.add(phiInst.getDef());
                        }
                        continue;
                    }

                    // 8
                    boolean fact8 = false;
                    for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                        var val = phiInst.getVals().get(i);
                        var label = phiInst.getLabels().get(i);
                        if (Excutable.contains(label2Block.get(label)) && val instanceof IRVariable
                                && V.get(val).a == 2) {
                            fact8 = true;
                            break;
                        }
                    }
                    if (fact8) {
                        if (V.get(phiInst.getDef()).a < 2) {
                            V.put(phiInst.getDef(), new Pair<>(2, null));
                            WorkListV.add(phiInst.getDef());
                        }
                        continue;
                    }
                }
                for (var inst : block.getInsts()) {
                    if (inst instanceof IRLoad) {
                        if (V.get(inst.getDef()).a < 2) {
                            V.put(inst.getDef(), new Pair<>(2, null));
                            WorkListV.add(inst.getDef());
                        }
                    } else if (inst instanceof IRCall && inst.getDef() != null) {
                        if (V.get(inst.getDef()).a < 2) {
                            V.put(inst.getDef(), new Pair<>(2, null));
                            WorkListV.add(inst.getDef());
                        }
                    } else if (inst instanceof IRArith) {
                        if (((((IRArith) inst).getLhs() instanceof IRVariable)
                                && (V.get(((IRArith) inst).getLhs()).a == 2))
                                || ((((IRArith) inst).getRhs() instanceof IRVariable)
                                        && (V.get(((IRArith) inst).getRhs()).a == 2))) {
                            if (V.get(inst.getDef()).a < 2) {
                                V.put(inst.getDef(), new Pair<>(2, null));
                                WorkListV.add(inst.getDef());
                            }
                        } else {
                            IREntity tmp = ((IRArith) inst).Innercompute(V);
                            if (tmp != null) {
                                if (V.get(inst.getDef()).a < 1) {
                                    V.put(inst.getDef(), new Pair<>(1, tmp));
                                    WorkListV.add(inst.getDef());
                                }
                            }
                        }
                    } else if (inst instanceof IRIcmp) {
                        if (((((IRIcmp) inst).getLhs() instanceof IRVariable)
                                && (V.get(((IRIcmp) inst).getLhs()).a == 2))
                                || ((((IRIcmp) inst).getRhs() instanceof IRVariable)
                                        && (V.get(((IRIcmp) inst).getRhs()).a == 2))) {
                            if (V.get(inst.getDef()).a < 2) {
                                V.put(inst.getDef(), new Pair<>(2, null));
                                WorkListV.add(inst.getDef());
                            }
                        } else {
                            IREntity tmp = ((IRIcmp) inst).Innercompute(V);
                            if (tmp != null) {
                                if (V.get(inst.getDef()).a < 1) {
                                    V.put(inst.getDef(), new Pair<>(1, tmp));
                                    WorkListV.add(inst.getDef());
                                }
                            }
                        }
                    } else if (inst instanceof IRGetelementptr) {
                        if (((((IRGetelementptr) inst).getPtr() instanceof IRVariable)
                                && (V.get(((IRGetelementptr) inst).getPtr()).a == 2))
                                || ((((IRGetelementptr) inst).getInfolist()
                                        .get(((IRGetelementptr) inst).getInfolist().size() - 1) instanceof IRVariable)
                                        && (V.get(((IRGetelementptr) inst).getInfolist()
                                                .get(((IRGetelementptr) inst).getInfolist().size() - 1)).a == 2))) {
                            if (V.get(inst.getDef()).a < 2) {
                                V.put(inst.getDef(), new Pair<>(2, null));
                                WorkListV.add(inst.getDef());
                            }

                        } else {
                            IREntity tmp = ((IRGetelementptr) inst).Innercompute(V);
                            if (tmp != null) {
                                if (V.get(inst.getDef()).a < 1) {
                                    V.put(inst.getDef(), new Pair<>(1, tmp));
                                    WorkListV.add(inst.getDef());
                                }
                            }
                        }
                    }
                }
                if (block.getSuccessors().size() == 1 && !Excutable.contains(block.getSuccessors().iterator().next())) {
                    Excutable.add(block.getSuccessors().iterator().next());
                    WorkListB.add(block.getSuccessors().iterator().next());
                    for (var succ : block.getSuccessors().iterator().next().getSuccessors()) {
                        if (Excutable.contains(succ)) {
                            WorkListB.add(succ);
                        }
                    }
                } else if (block.getReturnInst() instanceof IRBranch) {
                    var inst = block.getReturnInst();
                    if (Excutable.contains(block)) {
                        if (((IRBranch) inst).getCond() instanceof IRVariable) {
                            if (V.get(((IRBranch) inst).getCond()).a == 2) {
                                if (!Excutable.contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                    WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                    Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                    for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                            .getSuccessors()) {
                                        if (Excutable.contains(succ)) {
                                            WorkListB.add(succ);
                                        }
                                    }
                                }
                                if (!Excutable.contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                    WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                    Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                    for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                            .getSuccessors()) {
                                        if (Excutable.contains(succ)) {
                                            WorkListB.add(succ);
                                        }
                                    }
                                }
                            } else if (V.get(((IRBranch) inst).getCond()).a == 1) {
                                if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("1")) {
                                    if (!Excutable
                                            .contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                        WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                        Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                        for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                                .getSuccessors()) {
                                            if (Excutable.contains(succ)) {
                                                WorkListB.add(succ);
                                            }
                                        }
                                    }
                                } else if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("0")) {
                                    if (!Excutable
                                            .contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                        WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                        Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                        for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                                .getSuccessors()) {
                                            if (Excutable.contains(succ)) {
                                                WorkListB.add(succ);
                                            }
                                        }
                                    }
                                } else {
                                    throw new OPTError("Invalid value in branch cond in SCCP");
                                }
                            }
                        } else if (((IRBranch) inst).getCond() instanceof IRLiteral) {
                            if (((IRBranch) inst).getCond().getValue().equals("1")) {
                                if (!Excutable
                                        .contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                    WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                    Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                    for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                            .getSuccessors()) {
                                        if (Excutable.contains(succ)) {
                                            WorkListB.add(succ);
                                        }
                                    }
                                }
                            } else if (((IRBranch) inst).getCond().getValue().equals("0")) {
                                if (!Excutable
                                        .contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                    WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                    Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                    for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                            .getSuccessors()) {
                                        if (Excutable.contains(succ)) {
                                            WorkListB.add(succ);
                                        }
                                    }
                                }
                            } else {
                                throw new OPTError("Invalid value in branch cond in SCCP");
                            }
                        } else {
                            throw new OPTError("Invalid cond in branch in SCCP");
                        }
                    }
                }
            }
            while (!WorkListV.isEmpty()) {
                var var = WorkListV.iterator().next();
                WorkListV.remove(var);
                for (var inst : Var2Use.get(var)) {
                    if (inst instanceof IRPhi) {
                        var phiInst = (IRPhi) inst;
                        // 9
                        boolean fact9 = true;
                        IREntity entity = null;
                        for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                            var val = phiInst.getVals().get(i);
                            var label = phiInst.getLabels().get(i);
                            if (Excutable.contains(label2Block.get(label))) {
                                if (val instanceof IRVariable && V.get(val).a == 2) {
                                    fact9 = false;
                                    break;
                                } else if (val instanceof IRVariable && V.get(val).a == 0) {
                                    continue;
                                }
                                if (entity == null) {
                                    entity = val instanceof IRLiteral ? val : V.get(val).b;
                                } else if (!entity.equals(val instanceof IRLiteral ? val : V.get(val).b)) {
                                    fact9 = false;
                                    break;
                                }
                            }
                        }
                        fact9 = entity == null ? false : fact9;
                        if (fact9) {
                            if (V.get(phiInst.getDef()).a < 1) {
                                V.put(phiInst.getDef(), new Pair<>(1, entity));
                                WorkListV.add(phiInst.getDef());
                            }
                            continue;
                        }
                        // 6
                        boolean fact6 = false;
                        HashSet<IREntity> tmp = new HashSet<>();
                        for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                            var val = phiInst.getVals().get(i);
                            var label = phiInst.getLabels().get(i);
                            if (Excutable.contains(label2Block.get(label))
                                    && (val instanceof IRLiteral || V.get(val).a == 1)) {
                                if (!tmp.isEmpty() && !tmp.contains(val)) {
                                    fact6 = true;
                                    break;
                                }
                                tmp.add(val);
                            }
                        }
                        if (fact6) {
                            if (V.get(phiInst.getDef()).a < 2) {
                                V.put(phiInst.getDef(), new Pair<>(2, null));
                                WorkListV.add(phiInst.getDef());
                            }
                            continue;
                        }

                        // 8
                        boolean fact8 = false;
                        for (int i = 0; i < phiInst.getLabels().size(); ++i) {
                            var val = phiInst.getVals().get(i);
                            var label = phiInst.getLabels().get(i);
                            if (Excutable.contains(label2Block.get(label)) && val instanceof IRVariable
                                    && V.get(val).a == 2) {
                                fact8 = true;
                                break;
                            }
                        }
                        if (fact8) {
                            if (V.get(phiInst.getDef()).a < 2) {
                                V.put(phiInst.getDef(), new Pair<>(2, null));
                                WorkListV.add(phiInst.getDef());
                            }
                            continue;
                        }
                    } else {
                        if (inst instanceof IRLoad) {
                            if (V.get(inst.getDef()).a < 2) {
                                V.put(inst.getDef(), new Pair<>(2, null));
                                WorkListV.add(inst.getDef());
                            }
                        } else if (inst instanceof IRCall && inst.getDef() != null) {
                            if (V.get(inst.getDef()).a < 2) {
                                V.put(inst.getDef(), new Pair<>(2, null));
                                WorkListV.add(inst.getDef());
                            }
                        } else if (inst instanceof IRArith) {
                            if (((((IRArith) inst).getLhs() instanceof IRVariable)
                                    && (V.get(((IRArith) inst).getLhs()).a == 2))
                                    || ((((IRArith) inst).getRhs() instanceof IRVariable)
                                            && (V.get(((IRArith) inst).getRhs()).a == 2))) {
                                if (V.get(inst.getDef()).a < 2) {
                                    V.put(inst.getDef(), new Pair<>(2, null));
                                    WorkListV.add(inst.getDef());
                                }
                            } else {
                                IREntity tmp = ((IRArith) inst).Innercompute(V);
                                if (tmp != null) {
                                    if (V.get(inst.getDef()).a < 1) {
                                        V.put(inst.getDef(), new Pair<>(1, tmp));
                                        WorkListV.add(inst.getDef());
                                    }
                                }
                            }
                        } else if (inst instanceof IRIcmp) {
                            if (((((IRIcmp) inst).getLhs() instanceof IRVariable)
                                    && (V.get(((IRIcmp) inst).getLhs()).a == 2))
                                    || ((((IRIcmp) inst).getRhs() instanceof IRVariable)
                                            && (V.get(((IRIcmp) inst).getRhs()).a == 2))) {
                                if (V.get(inst.getDef()).a < 2) {
                                    V.put(inst.getDef(), new Pair<>(2, null));
                                    WorkListV.add(inst.getDef());
                                }
                            } else {
                                IREntity tmp = ((IRIcmp) inst).Innercompute(V);
                                if (tmp != null) {
                                    if (V.get(inst.getDef()).a < 1) {
                                        V.put(inst.getDef(), new Pair<>(1, tmp));
                                        WorkListV.add(inst.getDef());
                                    }
                                }
                            }
                        } else if (inst instanceof IRGetelementptr) {
                            if (((((IRGetelementptr) inst).getPtr() instanceof IRVariable)
                                    && (V.get(((IRGetelementptr) inst).getPtr()).a == 2))
                                    || ((((IRGetelementptr) inst).getInfolist()
                                            .get(((IRGetelementptr) inst).getInfolist().size()
                                                    - 1) instanceof IRVariable)
                                            && (V.get(((IRGetelementptr) inst).getInfolist()
                                                    .get(((IRGetelementptr) inst).getInfolist().size() - 1)).a == 2))) {
                                if (V.get(inst.getDef()).a < 2) {
                                    V.put(inst.getDef(), new Pair<>(2, null));
                                    WorkListV.add(inst.getDef());
                                }

                            } else {
                                IREntity tmp = ((IRGetelementptr) inst).Innercompute(V);
                                if (tmp != null) {
                                    if (V.get(inst.getDef()).a < 1) {
                                        V.put(inst.getDef(), new Pair<>(1, tmp));
                                        WorkListV.add(inst.getDef());
                                    }
                                }
                            }
                        } else if (inst instanceof IRBranch) {
                            if (!((IRBranch) inst).isJump() && Excutable.contains(Inst2Block.get(inst))) {
                                if(Inst2Block.get(inst).getLabelName().getLabel().equals("loop.3.condLabel"))
                                {
                                    int a=1;
                                }
                                if (((IRBranch) inst).getCond() instanceof IRVariable) {
                                    if (V.get(((IRBranch) inst).getCond()).a == 2) {
                                        if (!Excutable.contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                            WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                            Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                            for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                                    .getSuccessors()) {
                                                if (Excutable.contains(succ)) {
                                                    WorkListB.add(succ);
                                                }
                                            }
                                        }
                                        if (!Excutable.contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                            WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                            Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                            for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                                    .getSuccessors()) {
                                                if (Excutable.contains(succ)) {
                                                    WorkListB.add(succ);
                                                }
                                            }
                                        }
                                    } else if (V.get(((IRBranch) inst).getCond()).a == 1) {
                                        if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("1")) {
                                            if (!Excutable
                                                    .contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                                WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                                Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                                for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                                        .getSuccessors()) {
                                                    if (Excutable.contains(succ)) {
                                                        WorkListB.add(succ);
                                                    }
                                                }
                                            }
                                        } else if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("0")) {
                                            if (!Excutable
                                                    .contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                                WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                                Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                                for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                                        .getSuccessors()) {
                                                    if (Excutable.contains(succ)) {
                                                        WorkListB.add(succ);
                                                    }
                                                }
                                            }
                                        } else {
                                            throw new OPTError("Invalid value in branch cond in SCCP");
                                        }
                                    }
                                } else if (((IRBranch) inst).getCond() instanceof IRLiteral) {
                                    if (((IRBranch) inst).getCond().getValue().equals("1")) {
                                        if (!Excutable
                                                .contains(label2Block.get(((IRBranch) inst).getTrueLabel()))) {
                                            WorkListB.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                            Excutable.add(label2Block.get(((IRBranch) inst).getTrueLabel()));
                                            for (var succ : label2Block.get(((IRBranch) inst).getTrueLabel())
                                                    .getSuccessors()) {
                                                if (Excutable.contains(succ)) {
                                                    WorkListB.add(succ);
                                                }
                                            }
                                        }
                                    } else if (((IRBranch) inst).getCond().getValue().equals("0")) {
                                        if (!Excutable
                                                .contains(label2Block.get(((IRBranch) inst).getFalseLabel()))) {
                                            WorkListB.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                            Excutable.add(label2Block.get(((IRBranch) inst).getFalseLabel()));
                                            for (var succ : label2Block.get(((IRBranch) inst).getFalseLabel())
                                                    .getSuccessors()) {
                                                if (Excutable.contains(succ)) {
                                                    WorkListB.add(succ);
                                                }
                                            }
                                        }
                                    } else {
                                        throw new OPTError("Invalid value in branch cond in SCCP");
                                    }
                                } else {
                                    throw new OPTError("Invalid cond in branch in SCCP");
                                }
                            }
                        }
                    }
                }
            }
        }
        var Blockstmts = new ArrayList<IRBlock>();
        for (var block : func.getBlockstmts()) {
            if (Excutable.contains(block)) {
                var PhiList = new HashMap<IRVariable, IRPhi>();
                for (var phiInst : block.getPhiList().values()) {
                    if (V.get(phiInst.getDef()).a == 2) {
                        var tmp = new IRPhi(phiInst.getId(), phiInst.getDest(), phiInst.getType(),
                                new ArrayList<>(), new ArrayList<>());
                        for (int i = 0; i < phiInst.getVals().size(); ++i) {
                            var val = phiInst.getVals().get(i);
                            var label = phiInst.getLabels().get(i);
                            if (Excutable.contains(label2Block.get(label))) {
                                if (val instanceof IRVariable && V.get((IRVariable) val).a == 1) {
                                    tmp.getVals().add(V.get((IRVariable) val).b);
                                    tmp.getLabels().add(label);
                                } else {
                                    tmp.getVals().add(val);
                                    tmp.getLabels().add(label);
                                }
                            }
                        }
                        PhiList.put(phiInst.getDef(), tmp);
                    }
                }
                block.setPhiList(PhiList);
                var insts = new ArrayList<IRInst>();
                for (var inst : block.getInsts()) {
                    if (inst instanceof IRArith) {
                        if (V.get(((IRArith) inst).getDef()).a == 2) {
                            var tmp = new IRArith(inst.getId(), ((IRArith) inst).getDest(), ((IRArith) inst).getOp(),
                                    ((IRArith) inst).getType(), ((IRArith) inst).getLhs(), ((IRArith) inst).getRhs());
                            if (((IRArith) inst).getLhs() instanceof IRVariable
                                    && V.get(((IRArith) inst).getLhs()).a == 1) {
                                tmp.setLhs(V.get(((IRArith) inst).getLhs()).b);
                            }
                            if (((IRArith) inst).getRhs() instanceof IRVariable
                                    && V.get(((IRArith) inst).getRhs()).a == 1) {
                                tmp.setRhs(V.get(((IRArith) inst).getRhs()).b);
                            }
                            insts.add(tmp);
                        }
                    } else if (inst instanceof IRBranch) {
                        if (((IRBranch) inst).isJump()) {
                            insts.add(inst);
                        } else {
                            if (((IRBranch) inst).getCond() instanceof IRVariable) {
                                if (V.get(((IRBranch) inst).getCond()).a == 1) {
                                    if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("1")) {
                                        insts.add(new IRBranch(inst.getId(), ((IRBranch) inst).getTrueLabel()));
                                    } else if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("0")) {
                                        insts.add(new IRBranch(inst.getId(), ((IRBranch) inst).getFalseLabel()));
                                    } else {
                                        throw new OPTError("Invalid value in branch cond in SCCP");
                                    }
                                } else {
                                    insts.add(inst);
                                }
                            } else {
                                if (((IRBranch) inst).getCond().getValue().equals("1")) {
                                    insts.add(new IRBranch(inst.getId(), ((IRBranch) inst).getTrueLabel()));
                                } else if (((IRBranch) inst).getCond().getValue().equals("0")) {
                                    insts.add(new IRBranch(inst.getId(), ((IRBranch) inst).getFalseLabel()));
                                } else {
                                    throw new OPTError("Invalid value in branch cond in SCCP");
                                }
                            }
                        }
                    } else if (inst instanceof IRCall) {
                        var tmp = new IRCall(inst.getId(), ((IRCall) inst).getDest(), ((IRCall) inst).getType(),
                                ((IRCall) inst).getFuncName(), new ArrayList<>());
                        for (var arg : ((IRCall) inst).getArgs()) {
                            if (arg instanceof IRVariable && V.get((IRVariable) arg).a == 1) {
                                tmp.getArgs().add(V.get((IRVariable) arg).b);
                            } else {
                                tmp.getArgs().add(arg);
                            }
                        }
                        insts.add(tmp);
                    } else if (inst instanceof IRGetelementptr) {
                        if (V.get(((IRGetelementptr) inst).getDef()).a == 2) {
                            var tmp = new IRGetelementptr(inst.getId(), ((IRGetelementptr) inst).getDest(),
                                    ((IRGetelementptr) inst).getType(), ((IRGetelementptr) inst).getPtr(),
                                    new ArrayList<>());
                            for (var arg : ((IRGetelementptr) inst).getInfolist()) {
                                if (arg instanceof IRVariable && V.get((IRVariable) arg).a == 1) {
                                    tmp.getInfolist().add(V.get((IRVariable) arg).b);
                                } else {
                                    tmp.getInfolist().add(arg);
                                }
                            }
                            insts.add(tmp);
                        }
                    } else if (inst instanceof IRIcmp) {
                        if (V.get(((IRIcmp) inst).getDef()).a == 2) {
                            var tmp = new IRIcmp(inst.getId(), ((IRIcmp) inst).getDest(), ((IRIcmp) inst).getCond(),
                                    ((IRIcmp) inst).getType(), ((IRIcmp) inst).getLhs(), ((IRIcmp) inst).getRhs());
                            if (((IRIcmp) inst).getLhs() instanceof IRVariable
                                    && V.get(((IRIcmp) inst).getLhs()).a == 1) {
                                tmp.setLhs(V.get(((IRIcmp) inst).getLhs()).b);
                            }
                            if (((IRIcmp) inst).getRhs() instanceof IRVariable
                                    && V.get(((IRIcmp) inst).getRhs()).a == 1) {
                                tmp.setRhs(V.get(((IRIcmp) inst).getRhs()).b);
                            }
                            insts.add(tmp);
                        }
                    } else if (inst instanceof IRLoad) {
                        if (V.get(((IRLoad) inst).getDef()).a != 2) {
                            throw new OPTError("Invalid Load in SCCP");
                        }
                        insts.add(inst);
                    } else if (inst instanceof IRRet) {
                        if (((IRRet) inst).getValue() != null && ((IRRet) inst).getValue() instanceof IRVariable
                                && V.get(((IRRet) inst).getValue()).a == 1) {
                            insts.add(new IRRet(inst.getId(), V.get(((IRRet) inst).getValue()).b));
                        } else {
                            insts.add(inst);
                        }
                    } else if (inst instanceof IRStore) {
                        if (((IRStore) inst).getSrc() instanceof IRVariable
                                && V.get(((IRStore) inst).getSrc()).a == 1) {
                            insts.add(new IRStore(inst.getId(), ((IRStore) inst).getDest(),
                                    V.get(((IRStore) inst).getSrc()).b));
                        } else {
                            insts.add(inst);
                        }
                    } else {
                        throw new OPTError("Invalid inst in SCCP");
                    }
                }
                block.setInsts(insts);
                var inst = block.getReturnInst();
                if (inst instanceof IRBranch) {
                    if (((IRBranch) inst).isJump()) {
                        block.setReturnInst(inst);
                    } else {
                        if (((IRBranch) inst).getCond() instanceof IRVariable) {
                            if (V.get(((IRBranch) inst).getCond()).a == 1) {
                                if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("1")) {
                                    block.setReturnInst(new IRBranch(inst.getId(), ((IRBranch) inst).getTrueLabel()));
                                } else if (V.get(((IRBranch) inst).getCond()).b.getValue().equals("0")) {
                                    block.setReturnInst(new IRBranch(inst.getId(), ((IRBranch) inst).getFalseLabel()));
                                } else {
                                    throw new OPTError("Invalid value in branch cond in SCCP");
                                }
                            } else {
                                block.setReturnInst(inst);
                            }
                        } else {
                            if (((IRBranch) inst).getCond().getValue().equals("1")) {
                                block.setReturnInst(new IRBranch(inst.getId(), ((IRBranch) inst).getTrueLabel()));
                            } else if (((IRBranch) inst).getCond().getValue().equals("0")) {
                                block.setReturnInst(new IRBranch(inst.getId(), ((IRBranch) inst).getFalseLabel()));
                            } else {
                                throw new OPTError("Invalid value in branch cond in SCCP");
                            }
                        }
                    }
                } else if (inst instanceof IRRet) {
                    if (((IRRet) inst).getValue() != null && ((IRRet) inst).getValue() instanceof IRVariable
                            && V.get(((IRRet) inst).getValue()).a == 1) {
                        block.setReturnInst(new IRRet(inst.getId(), V.get(((IRRet) inst).getValue()).b));
                    } else {
                        block.setReturnInst(inst);
                    }
                } else {
                    throw new OPTError("Invalid return inst in SCCP");
                }
                Blockstmts.add(block);
            }
        }
        func.setBlockstmts(Blockstmts);
    }

    public void init(IRBlock block, HashSet<IRInst> WorkList) {
        for (var inst : block.getPhiList().values()) {
            Var2Pair.put(((IRPhi) inst).getDest(), new Pair<>(block, inst));
        }
        for (var inst : block.getInsts()) {
            if (!(inst instanceof IRLoad || inst instanceof IRStore || inst instanceof IRCall)) {
                WorkList.add(inst);
            }
            if (inst instanceof IRArith) {
                Var2Pair.put(((IRArith) inst).getDest(), new Pair<>(block, inst));
            } else if (inst instanceof IRPhi) {
                throw new OPTError("Phi in CodeMove");
            } else if (inst instanceof IRLoad) {
                Var2Pair.put(((IRLoad) inst).getDest(), new Pair<>(block, inst));
            } else if (inst instanceof IRGetelementptr) {
                Var2Pair.put(((IRGetelementptr) inst).getDest(), new Pair<>(block, inst));
            } else if (inst instanceof IRIcmp) {
                Var2Pair.put(((IRIcmp) inst).getDest(), new Pair<>(block, inst));
            } else if (inst instanceof IRCall) {
                if (((IRCall) inst).getDest() != null) {
                    Var2Pair.put(((IRCall) inst).getDest(), new Pair<>(block, inst));
                }
            } else if (inst instanceof IRAlloca) {
                throw new OPTError("Alloca in CodeMove");
            }
            // else{
            // throw new OPTError("Invalid inst in CodeMove");
            // }
        }
    }

    public void Collect(IRRoot root) throws BaseError {
        for (var def : root.getDefs()) {
            def.accept(this);
        }
        for (var func : root.getFuncs()) {
            func.accept(this);
        }
    }

    public void Run(IRRoot root) {
        var W = new LinkedList<IRVariable>();
        for (var entry : Var2Def.entrySet()) {
            var key = entry.getKey();
            W.add(key);
            if (Var2Use.get(key) == null) {
                Var2Use.put(key, new HashSet<IRInst>());
            }
        }
        for (var entry : Var2GDef.entrySet()) {
            var key = entry.getKey();
            W.add(key);
            if (Var2Use.get(key) == null) {
                Var2Use.put(key, new HashSet<IRInst>());
            }
        }
        while (!W.isEmpty()) {
            var v = W.poll();
            if (!Var2Use.get(v).isEmpty()) {
                continue;
            }
            if (v.isGlobal()) {
                var S = Var2GDef.get(v);
                if (S == null) {
                    continue;
                }
                root.RemoveDef(S);
                Var2GDef.remove(v);
            } else {
                var S = Var2Def.get(v);
                if (S == null) {
                    continue;
                }
                if (isSideEffect(S)) {
                    var block = Inst2Block.get(S);
                    block.RemoveInst(S);
                    Inst2Block.remove(S);
                    Var2Def.remove(v);
                    for (var x : S.getUses()) {
                        Var2Use.get(x).remove(S);
                        W.add((IRVariable) x);
                    }
                }
            }
        }
    }

    public boolean isSideEffect(IRInst inst) {
        if (inst instanceof IRCall) {
            return false;
        } else if (inst instanceof IRArith) {
            return true;
        } else if (inst instanceof IRLoad) {
            return true;
        } else if (inst instanceof IRPhi) {
            return true;
        } else if (inst instanceof IRIcmp) {
            return true;
        } else if (inst instanceof IRGetelementptr) {
            return true;
        } else {
            throw new OPTError("Invalid DefInst in DCE");
        }
    }

    @Override
    public OPTError visit(IRFuncDef funcDef) throws BaseError {
        for (var para : funcDef.getParams()) {
            Var2Def.put(para, null);
        }
        for (var block : funcDef.getBlockstmts()) {
            block.accept(this);
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRBlock block) {
        currentBlock = block;
        if(block.getLabelName().getLabel().equals("loop.3.condLabel"))
        {
            int a=1;
        }
        for (var inst : block.getPhiList().values()) {
            inst.accept(this);
        }
        for (var inst : block.getInsts()) {
            inst.accept(this);
        }
        block.getReturnInst().accept(this);
        return new OPTError();
    }

    @Override
    public OPTError visit(IRNode node) throws BaseError {
        return new OPTError("DCE: IRNode");
    }

    @Override
    public OPTError visit(IRGlobalDef node) throws BaseError {
        if (!(node.getVars().getType() instanceof IRStructType)) {
            // W.add(node.getVars());
            Var2GDef.put(node.getVars(), node);
        }
        return new OPTError("DCE: IRGlobalDef");
    }

    @Override
    public OPTError visit(IRStrDef node) throws BaseError {
        // W.add(node.getVars());
        Var2GDef.put(node.getVars(), node);
        return new OPTError("DCE: IRStrDef");
    }

    @Override
    public OPTError visit(IRAlloca node) throws BaseError {
        throw new OPTError("DCE: IRAlloca");
        // return new OPTError();
    }

    @Override
    public OPTError visit(IRArith node) throws BaseError {
        Var2Def.put(node.getDest(), node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRBranch node) throws BaseError {
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRCall node) throws BaseError {
        // SideEffectInst.add(node);
        Inst2Block.put(node, currentBlock);
        if (node.getDest() != null) {
            Var2Def.put(node.getDest(), node);
            Inst2Block.put(node, currentBlock);
        }
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRGetelementptr node) throws BaseError {
        Var2Def.put(node.getDest(), node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRRet node) throws BaseError {
        // SideEffectInst.add(node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        // if (node.getValue() != null && !(node.getValue() instanceof IRLiteral)) {
        // var unit = new HashSet<IRInst>();
        // unit.add(node);
        // Var2Use.put((IRVariable) node.getValue(), unit);
        // }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRLoad node) throws BaseError {
        Var2Def.put(node.getDest(), node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRPhi node) throws BaseError {
        Var2Def.put(node.getDest(), node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRIcmp node) throws BaseError {
        Var2Def.put(node.getDest(), node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IRStore node) throws BaseError {
        // SideEffectInst.add(node);
        Inst2Block.put(node, currentBlock);
        for (var use : node.getUses()) {
            var unit = Var2Use.get(use);
            if (unit == null) {
                unit = new HashSet<IRInst>();
                unit.add(node);
                Var2Use.put(use, unit);
            } else {
                unit.add(node);
            }
        }
        return new OPTError();
    }

    @Override
    public OPTError visit(IREntity node) throws BaseError {
        return new OPTError();
    }

    @Override
    public OPTError visit(IRVariable node) throws BaseError {
        return new OPTError();
    }

    @Override
    public OPTError visit(IRLiteral node) throws BaseError {
        return new OPTError();
    }

    @Override
    public OPTError visit(IROptBranch node) throws BaseError {
        return new OPTError();
    }
}
