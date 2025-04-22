package Compiler.Src.OPT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

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
import Compiler.Src.IR.Util.IRCounter;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.OPTError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

public class Inlining {
    private HashMap<String, Integer> Calltimes;
    private HashMap<String, Integer> Callednum;
    private HashMap<IRVariable, IREntity> param2name;
    private HashMap<String, IRFuncDef> name2func;
    private HashMap<Pair<IRFuncDef, String>, IRBlock> label2block;
    private int InlineCount;
    private IRPhi PhiInst;
    private IRLabel curBlock;

    // public boolean jud(IRFuncDef func)
    // {
    //     if(func.getBlockstmts().size()>4000)
    //     {
    //         return true;
    //     }
    //     return false;
    // }

    public void visit(IRRoot root) {
        Callednum = new HashMap<>();
        Calltimes = new HashMap<>();
        param2name = new HashMap<>();
        name2func = new HashMap<>();
        label2block = new HashMap<>();
        InlineCount = 0;
        for (var func : root.getFuncs()) {
            Callednum.put(func.getName(), 0);
            Calltimes.put(func.getName(), 0);
        }
        for (var func : root.getFuncs()) {
            calc(func);
        }
        boolean run = true;
        while (run) {
            run = false;
            for (var func : root.getFuncs()) {
                // if(jud(func))
                // {
                //     continue;
                // }
                if (Calltimes.get(func.getName()) == 0)
                    continue;
                run |= visit(func);
            }
        }
        // for (int i = 0; i < root.getFuncs().size(); ++i) {
        //     var func = root.getFuncs().get(i);
        //     if (Callednum.get(func.getName()) == 0 && !func.getName().equals("main")) {
        //         root.getFuncs().remove(i);
        //         i--;
        //     }
        // }
    }

    public void calc(IRFuncDef func) {
        int calltime = 0;
        for (var block : func.getBlockstmts()) {
            label2block.put(new Pair<IRFuncDef, String>(func, block.getLabelName().getLabel()), block);
            for (var inst : block.getInsts()) {
                if (inst instanceof IRCall) {
                    if (!Callednum.containsKey(((IRCall) inst).getFuncName())) {
                        continue;
                    }
                    calltime++;
                    Callednum.put(((IRCall) inst).getFuncName(),
                            Callednum.get(((IRCall) inst).getFuncName()) + 1);
                }
            }
        }
        Calltimes.put(func.getName(), calltime);
        name2func.put(func.getName(), func);
    }

    public boolean visit(IRFuncDef func) {
        boolean run = false;
        for (int i = 0; i < func.getBlockstmts().size(); i++) {
            var block = func.getBlockstmts().get(i);
            int InlineIndex = -1;
            for (var inst : block.getInsts()) {
                if (inst instanceof IRCall) {
                    var callInst = (IRCall) inst;
                    if (name2func.get(callInst.getFuncName()) != null
                            && (Callednum.get(callInst.getFuncName()) <= 3
                                    && Calltimes.get(callInst.getFuncName()) == 0
                                    && name2func.get(callInst.getFuncName()).getBlockstmts().size() <= 100)) {
                        InlineIndex = block.getInsts().indexOf(inst);
                        Callednum.put(callInst.getFuncName(), Callednum.get(callInst.getFuncName()) - 1);
                        Calltimes.put(func.getName(), Calltimes.get(func.getName()) - 1);
                        break;
                    }
                }
            }
            if (InlineIndex == -1)
                continue;
            run = true;
            var callinst = (IRCall) block.getInsts().get(InlineIndex);
            var targetfunc = name2func.get(callinst.getFuncName());
            if (!callinst.getType().equals(GlobalScope.irVoidType)) {
                PhiInst = new IRPhi(++InstCounter.InstCounter, callinst.getDest(), callinst.getType(),
                        new ArrayList<>(), new ArrayList<>());
            }
            var inlineBlocks = inline(targetfunc, func, callinst, block.getLoopDepth());
            var succBlock = new IRBlock(new IRLabel("inline." + InlineCount, block.getLoopDepth()),
                    block.getLoopDepth());
            label2block.put(new Pair<IRFuncDef, String>(func, succBlock.getLabelName().getLabel()), succBlock);
            var prevInsts = new ArrayList<IRInst>();
            for (int j = 0; j < InlineIndex; ++j) {
                prevInsts.add(block.getInsts().get(j));
            }
            var succInsts = new ArrayList<IRInst>();
            for (int j = InlineIndex + 1; j < block.getInsts().size(); ++j) {
                succInsts.add(block.getInsts().get(j));
            }
            succBlock.setReturnInst(block.getReturnInst());
            block.setReturnInst(new IRBranch(++InstCounter.InstCounter, inlineBlocks.get(0).getLabelName()));
            block.setInsts(prevInsts);
            succBlock.setInsts(succInsts);
            if (!callinst.getType().equals(GlobalScope.irVoidType)) {
                succBlock.getPhiList().put(callinst.getDest(), PhiInst);
            }
            inlineBlocks.add(succBlock);
            func.getBlockstmts().addAll(i + 1, inlineBlocks);
            if (succBlock.getReturnInst() instanceof IRBranch) {
                var branch = (IRBranch) succBlock.getReturnInst();
                if (branch.isJump()) {
                    ModPhi(label2block.get(new Pair<IRFuncDef, String>(func, branch.getTrueLabel().getLabel())),
                            block.getLabelName(),
                            succBlock.getLabelName());
                } else {
                    ModPhi(label2block.get(new Pair<IRFuncDef, String>(func, branch.getTrueLabel().getLabel())),
                            block.getLabelName(),
                            succBlock.getLabelName());
                    ModPhi(label2block.get(new Pair<IRFuncDef, String>(func, branch.getFalseLabel().getLabel())),
                            block.getLabelName(),
                            succBlock.getLabelName());
                }
            }
            ++InlineCount;
        }
        return run;
    }

    public void ModPhi(IRBlock block, IRLabel oldlabel, IRLabel newlabel) {
        for (var phi : block.getPhiList().values()) {
            for (int i = 0; i < phi.getLabels().size(); ++i) {
                if (phi.getLabels().get(i).equals(oldlabel)) {
                    phi.getLabels().set(i, newlabel);
                }
            }
        }
    }

    public ArrayList<IRBlock> inline(IRFuncDef func,IRFuncDef origin, IRCall callinst, int basedepth) {
        param2name = new HashMap<>();
        var inlineBlocks = new ArrayList<IRBlock>();
        for (int i = 0; i < func.getParams().size(); ++i) {
            param2name.put(func.getParams().get(i), callinst.getArgs().get(i));
        }
        for (var block : func.getBlockstmts()) {
            var newBlock = new IRBlock(new IRLabel(block.getLabelName().getLabel() + ".inline." + InlineCount,
                    block.getLoopDepth() + basedepth), block.getLoopDepth() + basedepth);
            // System.out.println(callinst.toString());
            // System.out.println(func.getName() + " inline " + newBlock.getLabelName().getLabel());
            label2block.put(new Pair<IRFuncDef, String>(origin, newBlock.getLabelName().getLabel()), newBlock);
            curBlock = newBlock.getLabelName();
            for (var pair : block.getPhiList().entrySet()) {
                newBlock.getPhiList().put((IRVariable) replaceEntity(pair.getKey()),
                        (IRPhi) replaceInsts(pair.getValue(), basedepth));
            }
            for (var inst : block.getInsts()) {
                newBlock.addInsts(replaceInsts(inst, basedepth));
            }
            newBlock.setReturnInst(replaceInsts(block.getReturnInst(), basedepth));
            inlineBlocks.add(newBlock);
        }
        return inlineBlocks;
    }

    public IRInst replaceInsts(IRInst inst, int basedepth) {
        if (inst instanceof IRAlloca) {
            return new IRAlloca(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRAlloca) inst).getDest()),
                    ((IRAlloca) inst).getType());
        } else if (inst instanceof IRArith) {
            return new IRArith(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRArith) inst).getDest()),
                    ((IRArith) inst).getOp(), ((IRArith) inst).getType(), replaceEntity(((IRArith) inst).getLhs()),
                    replaceEntity(((IRArith) inst).getRhs()));
        } else if (inst instanceof IRBranch) {
            if (((IRBranch) inst).isJump()) {
                return new IRBranch(++InstCounter.InstCounter,
                        new IRLabel(((IRBranch) inst).getTrueLabel().getLabel() + ".inline." + InlineCount,
                                ((IRBranch) inst).getTrueLabel().getLoopDepth() + basedepth));
            } else {
                return new IRBranch(++InstCounter.InstCounter, replaceEntity(((IRBranch) inst).getCond()),
                        new IRLabel(((IRBranch) inst).getTrueLabel().getLabel() + ".inline." + InlineCount,
                                ((IRBranch) inst).getTrueLabel().getLoopDepth() + basedepth),
                        new IRLabel(((IRBranch) inst).getFalseLabel().getLabel() + ".inline." + InlineCount,
                                ((IRBranch) inst).getFalseLabel().getLoopDepth() + basedepth));
            }
        } else if (inst instanceof IRCall) {
            var args = new ArrayList<IREntity>();
            for (var arg : ((IRCall) inst).getArgs()) {
                args.add(replaceEntity(arg));
            }
            if (((IRCall) inst).getDest() != null) {
                return new IRCall(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRCall) inst).getDest()),
                        ((IRCall) inst).getType(), ((IRCall) inst).getFuncName(), args);
            } else {
                return new IRCall(++InstCounter.InstCounter, ((IRCall) inst).getFuncName(), args);
            }
        } else if (inst instanceof IRGetelementptr) {
            var infoList = new ArrayList<IREntity>();
            for (var info : ((IRGetelementptr) inst).getInfolist()) {
                infoList.add(replaceEntity(info));
            }
            return new IRGetelementptr(++InstCounter.InstCounter,
                    (IRVariable) replaceEntity(((IRGetelementptr) inst).getDest()),
                    ((IRGetelementptr) inst).getType(), replaceEntity(((IRGetelementptr) inst).getPtr()), infoList);
        } else if (inst instanceof IRIcmp) {
            return new IRIcmp(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRIcmp) inst).getDest()),
                    ((IRIcmp) inst).getCond(), ((IRIcmp) inst).getType(), replaceEntity(((IRIcmp) inst).getLhs()),
                    replaceEntity(((IRIcmp) inst).getRhs()));
        } else if (inst instanceof IRLoad) {
            return new IRLoad(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRLoad) inst).getDest()),
                    (IRVariable) replaceEntity(((IRLoad) inst).getPtr()));
        } else if (inst instanceof IRPhi) {
            var vals = new ArrayList<IREntity>();
            for (var val : ((IRPhi) inst).getVals()) {
                vals.add(replaceEntity(val));
            }
            var labels = new ArrayList<IRLabel>();
            for (var label : ((IRPhi) inst).getLabels()) {
                labels.add(new IRLabel(label.getLabel() + ".inline." + InlineCount, label.getLoopDepth() + basedepth));
            }
            return new IRPhi(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRPhi) inst).getDest()),
                    ((IRPhi) inst).getType(), vals, labels);
        } else if (inst instanceof IRRet) {
            if (!((IRRet) inst).isVoidtype()) {
                PhiInst.getVals().add(replaceEntity(((IRRet) inst).getValue()));
                PhiInst.getLabels().add(curBlock);
            }
            return new IRBranch(++InstCounter.InstCounter, new IRLabel("inline." + InlineCount, basedepth));
        } else if (inst instanceof IRSelect) {
            throw new OPTError("IRSelect");
        } else if (inst instanceof IRStore) {
            return new IRStore(++InstCounter.InstCounter, (IRVariable) replaceEntity(((IRStore) inst).getDest()),
                    replaceEntity(((IRStore) inst).getSrc()));
        } else {
            throw new OPTError("Unknown IRInst");
        }
    }

    public IREntity replaceEntity(IREntity entity) {
        if (entity instanceof IRVariable && !((IRVariable) entity).isGlobal()) {
            if (((IRVariable) entity).getValue().endsWith(".param")) {
                return param2name.get(((IRVariable) entity));
            }
            return new IRVariable(entity.getType(), entity.getValue() + ".inline." + InlineCount);
        }
        return entity;
    }
}
