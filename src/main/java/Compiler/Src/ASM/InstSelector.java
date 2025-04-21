package Compiler.Src.ASM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import Compiler.Src.ASM.Entity.*;
import Compiler.Src.ASM.Node.*;
import Compiler.Src.ASM.Node.Global.*;
import Compiler.Src.ASM.Node.Inst.Arithmetic.*;
import Compiler.Src.ASM.Node.Inst.Control.*;
import Compiler.Src.ASM.Node.Inst.Memory.*;
import Compiler.Src.ASM.Node.Inst.Presudo.*;
import Compiler.Src.ASM.Node.Stmt.*;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.ASM.Util.*;
import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.IR.Node.*;
import Compiler.Src.IR.Node.Def.*;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.*;
import Compiler.Src.IR.Type.*;
import Compiler.Src.OPT.IRBranchOpt;
import Compiler.Src.Util.Error.ASMError;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.OPTError;

public class InstSelector extends ASMControl implements IRVisitor<ASMNode> {
    HashMap<IRVariable, ASMVirtualReg> IR2ASM;
    HashMap<ASMBlock, IRBlock> ASM2IR;

    @Override
    public ASMNode visit(IRNode node) throws BaseError {
        throw new ASMError("Unknown IR node type");
    }

    @Override
    public ASMNode visit(IRRoot node) throws BaseError {
        new IRBranchOpt().visit(node);
        curBlock = null;
        var root = new ASMRoot();
        IR2ASM = new HashMap<>();
        ASM2IR = new HashMap<>();
        for (var def : node.getDefs()) {
            if (def instanceof IRStrDef) {
                root.getStrs().add((ASMStrDef) def.accept(this));
            } else {
                var vars = (ASMVarDef) def.accept(this);
                if (vars != null) {
                    root.getVars().add(vars);
                }
            }
        }
        for (var func : node.getFuncs()) {
            root.getFuncs().add((ASMFuncDef) func.accept(this));
        }
        return root;
    }

    @Override
    public ASMNode visit(IRFuncDef node) throws BaseError {
        label2block = new TreeMap<>();
        var funcDef = new ASMFuncDef(node.getName(), node.getParams().size());
        curFunc = funcDef;
        funcBlocks = new ArrayList<>();
        counter = new ASMCounter();
        var paramCount = 0;
        var initStmt = new ASMBlock(new ASMLabel(node.getName()));
        initStmt.setLoopDepth(0);
        curBlock = initStmt;

        var offsetStack = (4 * (node.getParams().size() - 8) + 15) / 16 * 16;
        if (node.getParams().size() > 8) {
            funcDef.setTopPointer(new ASMVirtualReg(++ASMCounter.allocaCount));
        }
        for (var param : node.getParams()) {
            var paramInst = (ASMStmt) param.accept(this);
            var paramDest = paramInst.getDest();
            if (paramCount < 8) {
                initStmt.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, paramDest, getArgReg(paramCount)));
            } else {// 参数太多会爆炸，但是不太可能
                initStmt.addInst(new ASMLoad(++ASMCounter.InstCount, curBlock, "lw", paramDest,
                        offsetStack - 4 * (paramCount - 7), regs.getT1()));
                // initStmt.appendInsts(LoadAt(paramDest, offsetStack - 4 * (paramCount - 7)));
                // initStmt.appendInsts(StoreAt(regs.getT1(), 4 * ((ASMVirtualReg)
                // paramDest).getOffset()));
            }
            paramCount++;
        }

        funcBlocks.add(initStmt);
        curBlock = null;
        for (var block : node.getBlockstmts()) {
            CalcCFG(block);
        }
        for (var block : node.getBlockstmts()) {
            block.accept(this);
        }
        for (var block : funcBlocks) {
            if (block.equals(initStmt)) {
                continue;
            }
            var IRBlock = ASM2IR.get(block);
            // for (var irin : IRBlock.getLiveIn()) {
            // if(IR2ASM.get(irin)!=null)
            // {
            // block.getLiveIn().add(IR2ASM.get(irin));
            // }
            // }
            for (var irout : IRBlock.getLiveOut()) {
                if (IR2ASM.get(irout) != null) {
                    block.getLiveOut().add(IR2ASM.get(irout));
                }
            }
        }

        for (var block : node.getBlockstmts()) {
            RmvPhi(block);
        }
        for (var block : funcBlocks) {
            block.PhiMove_Formal(this);
        }
        initStmt.getSucc().add(funcBlocks.get(1));
        for (int i = 0; i < node.getParams().size(); ++i) {
            initStmt.getLiveOut().add(IR2ASM.get(node.getParams().get(i)));
        }
        for (var succ : initStmt.getSucc()) {
            initStmt.getLiveOut().addAll(succ.getLiveOut());
        }
        funcDef.setBlocks(funcBlocks);
        Formolize(funcDef);
        return funcDef;
    }

    public void RmvPhi(IRBlock node) {
        var block = label2block.get(node.getLabelName().getLabel());
        // Phi remove
        var label2new = new TreeMap<String, String>();
        for (var phi : node.getPhiList().values()) {
            var destInst = (ASMStmt) phi.getDest().accept(this);
            for (var label : phi.getLabels()) {
                var blockLabel = label.getLabel();
                if (!label2block.containsKey(blockLabel)) {
                    throw new ASMError("ASM: phi label not found");
                }
                var predBlock = label2block.get(blockLabel);
                var src = phi.getVals().get(phi.getLabels().indexOf(label));
                if (predBlock.getSuccessor().size() == 1) {
                    predBlock.getPhiStmt().appendInsts(destInst);
                    ASMVirtualReg SrcDest;
                    // if (src instanceof IRVariable) {
                    var SrcInst = (ASMStmt) src.accept(this);
                    predBlock.getPhiStmt().appendInsts(SrcInst);
                    SrcDest = (ASMVirtualReg) SrcInst.getDest();
                    // } else {
                    // SrcDest = new ASMVirtualReg("Czar", IRLiteral2Int((IRLiteral) src));
                    // }
                    // predBlock.getLiveOut().add(SrcDest);
                    // block.getLiveIn().add(SrcDest);
                    if (predBlock.getSrc2dest().containsKey(SrcDest)) {
                        predBlock.getSrc2dest().get(SrcDest).add((ASMVirtualReg) destInst.getDest());
                    } else {
                        predBlock.getSrc2dest().put(SrcDest,
                                new ArrayList<>(Arrays.asList((ASMVirtualReg) destInst.getDest())));
                    }
                } else {
                    ASMBlock midBlock;
                    if (!label2new.containsKey(blockLabel)) {
                        midBlock = new ASMBlock(new ASMLabel(
                                node.getLabelName().getLabel() + "." + blockLabel + ".PhiCreate."
                                        + (++CreateblockCnt)));
                        midBlock.setLoopDepth(predBlock.getLoopDepth());
                        var predindex=funcBlocks.indexOf(predBlock);
                        funcBlocks.add(predindex+1,midBlock);
                        label2new.put(blockLabel, midBlock.getLabel().getLabel());
                        label2block.put(midBlock.getLabel().getLabel(), midBlock);

                        // midBlocks.add(midBlock);
                        // for (var irin : ASM2IR.get(block).getLiveInPhi().get(label)) {
                        // midBlock.getLiveIn().add(IR2ASM.get(irin));
                        // }
                        // midBlock.getLiveOut().addAll(block.getLiveIn());

                        IRBlock PredIRBlock = null;
                        for (var pred : node.getPredecessors()) {
                            if (pred.getLabelName().equals(label)) {
                                PredIRBlock = pred;
                                break;
                            }
                        }
                        HashSet<IRVariable> predOut = new HashSet<>(PredIRBlock.getLiveOut());
                        for (var phiInst : node.getPhiList().values()) {
                            for (int i = 0; i < phiInst.getVals().size(); ++i) {
                                var philabel = phiInst.getLabels().get(i);
                                var val = phiInst.getVals().get(i);
                                if (PredIRBlock.getLabelName().equals(philabel) && val instanceof IRVariable) {
                                    predOut.remove(val);
                                    predOut.add(phiInst.getDef());
                                    break;
                                }
                            }
                        }
                        for (var out : predOut) {
                            if (IR2ASM.get(out) != null) {
                                midBlock.getLiveOut().add(IR2ASM.get(out));
                            } else if (!out.isGlobal()) {
                                continue;//due to IRBranchOpt
                                // throw new OPTError("Unknown var");
                            }
                        }

                        predBlock.replaceLabel(block.getLabel().getLabel(), midBlock.getLabel().getLabel());
                        midBlock.getSuccessor().add(blockLabel);
                        var jumpInst = new ASMStmt();
                        jumpInst.addInst(new ASMJump(++ASMCounter.InstCount, curBlock, node.getLabelName().getLabel()));
                        midBlock.setReturnInst(jumpInst);
                    } else {
                        midBlock = label2block.get(label2new.get(blockLabel));
                    }
                    midBlock.getPhiStmt().appendInsts(destInst);
                    ASMVirtualReg SrcDest;
                    // if (src instanceof IRVariable) {
                    var SrcInst = (ASMStmt) src.accept(this);
                    midBlock.getPhiStmt().appendInsts(SrcInst);
                    SrcDest = (ASMVirtualReg) SrcInst.getDest();

                    // midBlock.getLiveOut().add(SrcDest);
                    // block.getLiveIn().add(SrcDest);

                    if (midBlock.getSrc2dest().containsKey(SrcDest)) {
                        midBlock.getSrc2dest().get(SrcDest)
                                .add((ASMVirtualReg) destInst.getDest());
                    } else {
                        midBlock.getSrc2dest().put(SrcDest,
                                new ArrayList<>(Arrays.asList((ASMVirtualReg) destInst.getDest())));
                    }
                }
            }
        }
    }

    public void CalcCFG(IRBlock node) {
        var block = new ASMBlock(new ASMLabel(node.getLabelName().getLabel()));
        block.setLoopDepth(node.getLoopDepth());
        label2block.put(node.getLabelName().getLabel(), block);
        if (node.getReturnInst() instanceof IRRet) {
            block.setSuccessor(new ArrayList<>());
        } else if (node.getReturnInst() instanceof IRBranch) {
            if (((IRBranch) node.getReturnInst()).isJump()) {
                block.setSuccessor(new ArrayList<String>(
                        Arrays.asList(((IRBranch) node.getReturnInst()).getTrueLabel().getLabel())));
            } else {
                var trueLabel = ((IRBranch) node.getReturnInst()).getTrueLabel().getLabel();
                var falseLabel = ((IRBranch) node.getReturnInst()).getFalseLabel().getLabel();
                block.setSuccessor(new ArrayList<>());
                block.getSuccessor().add(trueLabel);
                block.getSuccessor().add(falseLabel);
            }
        } 
        else if (node.getReturnInst() instanceof IROptBranch) {
                var trueLabel = ((IROptBranch) node.getReturnInst()).getTrueLabel().getLabel();
                var falseLabel = ((IROptBranch) node.getReturnInst()).getFalseLabel().getLabel();
                block.setSuccessor(new ArrayList<>());
                block.getSuccessor().add(trueLabel);
                block.getSuccessor().add(falseLabel);
        }
        else {
            throw new ASMError("Unknown return inst");
        }
    }

    @Override
    public ASMNode visit(IRStrDef node) throws BaseError {
        var str = node.getValue_old()
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\0", "")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
        var StrDef = new ASMStrDef(node.getVars().getValue().substring(1), str);
        return StrDef;
    }

    @Override
    public ASMNode visit(IRBlock node) throws BaseError {
        var block = label2block.get(node.getLabelName().getLabel());
        ASM2IR.put(block, node);
        curBlock = block;

        // Collect
        for (var phi : node.getPhiList().values()) {
            phi.getDef().accept(this);
        }

        for (var stmt : node.getInsts()) {
            block.appendInsts((ASMStmt) stmt.accept(this));
        }
        var returnInst = (ASMStmt) node.getReturnInst().accept(this);
        if (returnInst.getInsts().size() == 0) {
            int a = 1;
        }
        block.setReturnInst(returnInst);
        funcBlocks.add(block);
        curBlock = null;
        return new ASMStmt();
    }

    @Override
    public ASMNode visit(IRGlobalDef node) throws BaseError {
        if (node.getVars().getType() instanceof IRStructType) {
            return null;
        }
        return new ASMVarDef(node.getVars().getValue().substring(1), 0);
    }

    @Override
    public ASMNode visit(IRAlloca node) throws BaseError {
        throw new OPTError("Alloca should be eliminated");
        // var InstList = new ASMStmt();
        // var DestInst = (ASMStmt) node.getDest().accept(this);
        // var allocaDest = DestInst.getDest();
        // if (allocaDest instanceof ASMPhysicalReg) {
        // throw new ASMError("not supose to use this in Naive ASM");
        // }
        // InstList.addInst(
        // new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT0(), 4 *
        // curFunc.getStackSize()));
        // InstList.addInst(
        // new ASMArithR(++ASMCounter.InstCount, curBlock, "add", regs.getT0(),
        // regs.getSp(), regs.getT0()));
        // InstList.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, allocaDest,
        // regs.getT0()));
        // return InstList;
    }

    @Override
    public ASMNode visit(IRArith node) throws BaseError {
        var InstList = new ASMStmt();
        var DestInst = (ASMStmt) node.getDest().accept(this);
        InstList.appendInsts(DestInst);
        var Dest = DestInst.getDest();
        InstList.setDest(Dest);
        var op = node.getOp();
        if (ValidImm(node.getLhs()) && ValidImm(node.getRhs())) {
            var lhs = IRLiteral2Int((IRLiteral) node.getLhs());
            var rhs = IRLiteral2Int((IRLiteral) node.getRhs());
            switch (op) {
                case "add" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs + rhs));
                }
                case "sub" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs - rhs));
                }
                case "mul" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs * rhs));
                }
                case "sdiv" -> {
                    if (rhs == 0) {
                        InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, 0));
                    } else {
                        InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs / rhs));
                    }
                }
                case "srem" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs % rhs));
                }
                case "shl" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs << rhs));
                }
                case "ashr" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs >> rhs));
                }
                case "and" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs & rhs));
                }
                case "or" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs | rhs));
                }
                case "xor" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs ^ rhs));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else if (ValidImm(node.getRhs())) {
            var rhs = IRLiteral2Int((IRLiteral) node.getRhs());
            var lhsInst = (ASMStmt) node.getLhs().accept(this);
            InstList.appendInsts(lhsInst);
            var lhsDest = lhsInst.getDest();
            switch (op) {
                case "add" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", Dest, lhsDest, rhs));
                }
                case "sub" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", Dest, lhsDest, -rhs));
                }
                case "mul" -> {
                    var rhsInst = (ASMStmt) node.getRhs().accept(this);
                    InstList.appendInsts(rhsInst);
                    var rhsDest = rhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "mul", Dest, lhsDest, rhsDest));
                }
                case "sdiv" -> {
                    var rhsInst = (ASMStmt) node.getRhs().accept(this);
                    InstList.appendInsts(rhsInst);
                    var rhsDest = rhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "div", Dest, lhsDest, rhsDest));
                }
                case "srem" -> {
                    var rhsInst = (ASMStmt) node.getRhs().accept(this);
                    InstList.appendInsts(rhsInst);
                    var rhsDest = rhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "rem", Dest, lhsDest, rhsDest));
                }
                case "shl" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slli", Dest, lhsDest, rhs));
                }
                case "ashr" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "srai", Dest, lhsDest, rhs));
                }
                case "and" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "andi", Dest, lhsDest, rhs));
                }
                case "or" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "ori", Dest, lhsDest, rhs));
                }
                case "xor" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", Dest, lhsDest, rhs));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else if (ValidImm(node.getLhs())) {
            var lhs = IRLiteral2Int((IRLiteral) node.getLhs());
            var rhsInst = (ASMStmt) node.getRhs().accept(this);
            InstList.appendInsts(rhsInst);
            var rhsDest = rhsInst.getDest();
            switch (op) {
                case "add" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", Dest, rhsDest, lhs));
                }
                case "sub" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "neg", tmpDest, rhsDest));
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", Dest, tmpDest, lhs));
                }
                case "mul" -> {
                    var lhsInst = (ASMStmt) node.getLhs().accept(this);
                    InstList.appendInsts(lhsInst);
                    var lhsDest = lhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "mul", Dest, lhsDest, rhsDest));
                }
                case "sdiv" -> {
                    var lhsInst = (ASMStmt) node.getLhs().accept(this);
                    InstList.appendInsts(lhsInst);
                    var lhsDest = lhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "div", Dest, lhsDest, rhsDest));
                }
                case "srem" -> {
                    var lhsInst = (ASMStmt) node.getLhs().accept(this);
                    InstList.appendInsts(lhsInst);
                    var lhsDest = lhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "rem", Dest, lhsDest, rhsDest));
                }
                case "shl" -> {
                    var lhsInst = (ASMStmt) node.getLhs().accept(this);
                    InstList.appendInsts(lhsInst);
                    var lhsDest = lhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sll", Dest, lhsDest, rhsDest));
                }
                case "ashr" -> {
                    var lhsInst = (ASMStmt) node.getLhs().accept(this);
                    InstList.appendInsts(lhsInst);
                    var lhsDest = lhsInst.getDest();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sra", Dest, lhsDest, rhsDest));
                }
                case "and" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "andi", Dest, rhsDest, lhs));
                }
                case "or" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "ori", Dest, rhsDest, lhs));
                }
                case "xor" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", Dest, rhsDest, lhs));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else {
            var lhsInst = (ASMStmt) node.getLhs().accept(this);
            var rhsInst = (ASMStmt) node.getRhs().accept(this);
            InstList.appendInsts(lhsInst);
            InstList.appendInsts(rhsInst);
            var lhsDest = lhsInst.getDest();
            var rhsDest = rhsInst.getDest();
            switch (op) {
                case "add" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "add", Dest, lhsDest, rhsDest));
                }
                case "sub" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sub", Dest, lhsDest, rhsDest));
                }
                case "mul" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "mul", Dest, lhsDest, rhsDest));
                }
                case "sdiv" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "div", Dest, lhsDest, rhsDest));
                }
                case "srem" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "rem", Dest, lhsDest, rhsDest));
                }
                case "shl" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sll", Dest, lhsDest, rhsDest));
                }
                case "ashr" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "sra", Dest, lhsDest, rhsDest));
                }
                case "and" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "and", Dest, lhsDest, rhsDest));
                }
                case "or" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "or", Dest, lhsDest, rhsDest));
                }
                case "xor" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "xor", Dest, lhsDest, rhsDest));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        }
        // InstList.appendInsts(StoreAt(regs.getA0(), 4 * ((ASMVirtualReg)
        // DestInst.getDest()).getOffset()));
        return InstList;
    }

    @Override
    public ASMNode visit(IRIcmp node) throws BaseError {
        var InstList = new ASMStmt();
        var DestInst = (ASMStmt) node.getDest().accept(this);
        InstList.appendInsts(DestInst);
        var Dest = DestInst.getDest();
        InstList.setDest(Dest);
        var cond = node.getCond();
        if (ValidImm(node.getLhs()) && ValidImm(node.getRhs())) {
            var lhs = IRLiteral2Int((IRLiteral) node.getLhs());
            var rhs = IRLiteral2Int((IRLiteral) node.getRhs());
            switch (cond) {
                case "eq" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs == rhs ? 1 : 0));
                }
                case "ne" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs != rhs ? 1 : 0));
                }
                case "slt" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs < rhs ? 1 : 0));
                }
                case "sgt" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs > rhs ? 1 : 0));
                }
                case "sle" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs <= rhs ? 1 : 0));
                }
                case "sge" -> {
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, Dest, lhs >= rhs ? 1 : 0));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else if (ValidImm(node.getRhs())) {
            var rhs = IRLiteral2Int((IRLiteral) node.getRhs());
            var lhsInst = (ASMStmt) node.getLhs().accept(this);
            InstList.appendInsts(lhsInst);
            var lhsDest = lhsInst.getDest();
            // InstList.appendInsts(LoadAt(regs.getA1(), 4 * ((ASMVirtualReg)
            // lhsDest).getOffset()));
            switch (cond) {
                case "eq" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", tmpDest, lhsDest, rhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "ne" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", tmpDest, lhsDest, rhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "snez", Dest, tmpDest));
                }
                case "slt" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", Dest, lhsDest, rhs));
                }
                case "sgt" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", tmpDest, lhsDest, rhs + 1));
                    InstList.addInst(new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "sle" -> {
                    InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", Dest, lhsDest, rhs + 1));
                }
                case "sge" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", tmpDest, lhsDest, rhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else if (ValidImm(node.getLhs())) {
            var lhs = IRLiteral2Int((IRLiteral) node.getLhs());
            var rhsInst = (ASMStmt) node.getRhs().accept(this);
            InstList.appendInsts(rhsInst);
            var rhsDest = rhsInst.getDest();
            // InstList.appendInsts(LoadAt(regs.getA2(), 4 * ((ASMVirtualReg)
            // rhsDest).getOffset()));
            switch (cond) {
                case "eq" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", tmpDest, rhsDest, lhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "ne" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "xori", tmpDest, rhsDest, lhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "snez", Dest, tmpDest));
                }
                case "slt" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", tmpDest, rhsDest, lhs + 1));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "sgt" -> {
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", Dest, rhsDest, lhs));
                }
                case "sle" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(
                            new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", tmpDest, rhsDest, lhs));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "sge" -> {
                    InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "slti", Dest, rhsDest, lhs + 1));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        } else {
            var lhsInst = (ASMStmt) node.getLhs().accept(this);
            var rhsInst = (ASMStmt) node.getRhs().accept(this);
            InstList.appendInsts(lhsInst);
            InstList.appendInsts(rhsInst);
            var lhsDest = lhsInst.getDest();
            var rhsDest = rhsInst.getDest();
            switch (cond) {
                case "eq" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "xor", tmpDest, lhsDest, rhsDest));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "ne" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "xor", tmpDest, lhsDest, rhsDest));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "snez", Dest, tmpDest));
                }
                case "slt" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "slt", Dest, lhsDest, rhsDest));
                }
                case "sgt" -> {
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "slt", Dest, rhsDest, lhsDest));
                }
                case "sle" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "slt", tmpDest, rhsDest, lhsDest));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                case "sge" -> {
                    // var tmpDest = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmpDest = regs.getT0();
                    InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "slt", tmpDest, lhsDest, rhsDest));
                    InstList.addInst(
                            new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", Dest, tmpDest));
                }
                default -> throw new ASMError("Unknown Binary operation");
            }
        }
        // InstList.appendInsts(StoreAt(regs.getA0(), 4 * ((ASMVirtualReg)
        // DestInst.getDest()).getOffset()));
        return InstList;
    }

    @Override
    public ASMNode visit(IRBranch node) throws BaseError {
        var InstList = new ASMStmt();
        if (node.isJump()) {
            InstList.addInst(new ASMJump(++ASMCounter.InstCount, curBlock, node.getTrueLabel().getLabel()));
        } else {
            var condInst = (ASMStmt) node.getCond().accept(this);
            var condDest = condInst.getDest();
            String tmpLabel = curBlock.getLabel().getLabel() + ".tmp.label";
            InstList.appendInsts(condInst);
            // var condDest_true = new ASMVirtualReg(++ASMCounter.allocaCount);
            var condDest_true = regs.getT0();
            InstList.addInst(new ASMUnarry(++ASMCounter.InstCount, curBlock, "seqz", condDest_true, condDest));
            InstList.addInst(
                    new ASMBeq(++ASMCounter.InstCount, curBlock, condDest_true, null, tmpLabel, 0));
            InstList.addInst(new ASMJump(++ASMCounter.InstCount, curBlock, node.getFalseLabel().getLabel()));
            curBlock.setJlabel(new ASMLabel(tmpLabel));
            curBlock.setJump(new ASMJump(++ASMCounter.InstCount, curBlock, node.getTrueLabel().getLabel()));
        }
        return InstList;
    }

    @Override
    public ASMNode visit(IROptBranch node) throws BaseError {
        var InstList = new ASMStmt();
        String tmpLabel = curBlock.getLabel().getLabel() + ".tmp.label";
        curBlock.setJlabel(new ASMLabel(tmpLabel));
        curBlock.setJump(new ASMJump(++ASMCounter.InstCount, curBlock, node.getTrueLabel().getLabel()));
        if (node.isEq) {
            var lhsInst = (ASMStmt) node.getCond1().accept(this);
            var rhsInst = (ASMStmt) node.getCond2().accept(this);
            InstList.appendInsts(lhsInst);
            InstList.appendInsts(rhsInst);
            var lhsDest = lhsInst.getDest();
            var rhsDest = rhsInst.getDest();
            InstList.addInst(new ASMBeq(++ASMCounter.InstCount, curBlock, lhsDest, rhsDest, tmpLabel, 1));
            InstList.addInst(new ASMJump(++ASMCounter.InstCount, curBlock, node.getFalseLabel().getLabel()));
            return InstList;
        } else {
            var lhsInst = (ASMStmt) node.getCond1().accept(this);
            var rhsInst = (ASMStmt) node.getCond2().accept(this);
            InstList.appendInsts(lhsInst);
            InstList.appendInsts(rhsInst);
            var lhsDest = lhsInst.getDest();
            var rhsDest = rhsInst.getDest();
            InstList.addInst(new ASMBeq(++ASMCounter.InstCount, curBlock, lhsDest, rhsDest, tmpLabel, 2));
            InstList.addInst(new ASMJump(++ASMCounter.InstCount, curBlock, node.getFalseLabel().getLabel()));
            return InstList;
        }
    }

    @Override
    public ASMNode visit(IRCall node) throws BaseError {
        var InstList = new ASMStmt();
        int argNum = 0;
        int offset = 0;

        var ComputeInst = new ASMStmt();
        if (node.getFuncName().equals("__malloc_array") || node.getFuncName().equals("_malloc")) {
            if (node.getArgs().size() > 2) {
                throw new ASMError("malloc should have 1 or 2 args");
            }
            if (node.getFuncName().equals("__malloc_array")) {
                var arg1 = node.getArgs().get(0);
                if (arg1 instanceof IRLiteral) {
                    InstList.addInst(
                            new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(), IRLiteral2Int((IRLiteral) arg1)));
                } else {
                    var argInst = (ASMStmt) arg1.accept(this);
                    ComputeInst.appendInsts(argInst);
                    InstList.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, regs.getA0(), argInst.getDest()));
                }
                ++argNum;
                var arg2 = node.getArgs().get(1);
                if (arg2 instanceof IRVariable) {
                    throw new ASMError("arg should be literal");
                } else {
                    var value = IRLiteral2Int((IRLiteral) arg2);
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA1(),
                            value % 4 != 0 ? value + 4 - value % 4 : value));
                }
                ++argNum;
            } else {
                var arg1 = node.getArgs().get(0);
                if (arg1 instanceof IRVariable) {
                    throw new ASMError("arg should be literal");
                } else {
                    var value = IRLiteral2Int((IRLiteral) arg1);
                    InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(),
                            value % 4 != 0 ? value + 4 - value % 4 : value));
                }
                ++argNum;
            }
        } else {
            for (var arg : node.getArgs()) {
                if (argNum < 8) {
                    if (arg instanceof IRVariable) {
                        var argInst = (ASMStmt) arg.accept(this);
                        ComputeInst.appendInsts(argInst);
                        InstList.addInst(
                                new ASMMove(++ASMCounter.InstCount, curBlock, getArgReg(argNum), argInst.getDest()));
                    } else {
                        InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, getArgReg(argNum),
                                IRLiteral2Int((IRLiteral) arg)));
                    }
                } else {// 上千参数会超addi，但传参应当不会这么多
                    ++offset;
                    var argInst = (ASMStmt) arg.accept(this);
                    ComputeInst.appendInsts(argInst);
                    InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "sw", argInst.getDest(),
                            -4 * offset, regs.getSp()));
                }
                ++argNum;
            }
        }
        InstList.appendInsts(0, ComputeInst);
        // InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getT1(),
        // 0));
        // InstList.appendInsts(StoreInst);
        var offsetStack = (4 * offset + 15) / 16 * 16;
        if (offset != 0) {
            InstList.addInst(
                    new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getSp(), regs.getSp(), -offsetStack));
        }
        InstList.addInst(
                new ASMCall(++ASMCounter.InstCount, curBlock, node.getFuncName(), node.getDest() != null, argNum));
        if (offset != 0) {
            InstList.addInst(
                    new ASMArithI(++ASMCounter.InstCount, curBlock, "addi", regs.getSp(), regs.getSp(), offsetStack));
        }
        // InstList.appendInsts(LoadInst);
        if (node.getDest() != null) {
            var destInst = (ASMStmt) node.getDest().accept(this);
            var dest = destInst.getDest();
            InstList.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, dest, regs.getA0()));
        }

        // // get back a0-a7
        // for (int i = 0; i < 8; ++i) {
        // var tmpCompute = StoreLinker.get(getArgReg(i));
        // InstList.addInst(new ASMLoad(++ASMCounter.InstCount, curBlock, "lw",
        // getArgReg(i), 0, tmpCompute));
        // }

        return InstList;
    }

    @Override
    public ASMNode visit(IRGetelementptr node) throws BaseError {
        var InstList = new ASMStmt();
        var DestInst = (ASMStmt) node.getDest().accept(this);
        InstList.appendInsts(DestInst);
        var DestDest = DestInst.getDest();
        var PtrInst = (ASMStmt) node.getPtr().accept(this);
        InstList.appendInsts(PtrInst);
        var PtrDest = PtrInst.getDest();
        var IndexEntity = (IREntity) node.getInfolist().get(node.getInfolist().size() - 1);
        var IndexInst = (ASMStmt) IndexEntity.accept(this);
        InstList.appendInsts(IndexInst);
        var IndexDest = IndexInst.getDest();
        // var tmpDest1 = new ASMVirtualReg(++ASMCounter.allocaCount);
        var tmpDest1 = regs.getT0();
        // var tmpDest2 = new ASMVirtualReg(++ASMCounter.allocaCount);
        var tmpDest2 = regs.getT1();
        InstList.addInst(new ASMArithI(++ASMCounter.InstCount, curBlock, "slli", tmpDest2, IndexDest, 2));
        InstList.addInst(new ASMArithR(++ASMCounter.InstCount, curBlock, "add", tmpDest1, PtrDest, tmpDest2));
        InstList.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, DestDest, tmpDest1));
        return InstList;
    }

    @Override
    public ASMNode visit(IRRet node) throws BaseError {
        var InstList = new ASMStmt();
        if (node.isVoidtype()) {
            InstList.addInst(new ASMRet(++ASMCounter.InstCount, curBlock));
        } else {
            if (node.getValue() instanceof IRVariable) {
                var DestInst = (ASMStmt) node.getValue().accept(this);
                InstList.appendInsts(DestInst);
                InstList.addInst(new ASMMove(++ASMCounter.InstCount, curBlock, regs.getA0(), DestInst.getDest()));
                // InstList.appendInsts(LoadAt(regs.getA0(), 4 * ((ASMVirtualReg)
                // DestInst.getDest()).getOffset()));
                InstList.addInst(new ASMRet(++ASMCounter.InstCount, curBlock));
            } else if (node.getValue() instanceof IRLiteral) {
                var Literal = IRLiteral2Int((IRLiteral) node.getValue());
                InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, regs.getA0(), Literal));
                InstList.addInst(new ASMRet(++ASMCounter.InstCount, curBlock));
            } else {
                throw new ASMError("Return value Error");
            }
        }
        return InstList;
    }

    @Override
    public ASMNode visit(IRLoad node) throws BaseError {
        var InstList = new ASMStmt();
        var DestInst = (ASMStmt) node.getDest().accept(this);
        var PtrInst = (ASMStmt) node.getPtr().accept(this);
        InstList.appendInsts(DestInst);
        InstList.appendInsts(PtrInst);
        var Dest = DestInst.getDest();
        var PtrDest = PtrInst.getDest();
        InstList.addInst(new ASMLoad(++ASMCounter.InstCount, curBlock, "lw", Dest, 0, PtrDest));
        return InstList;
    }

    @Override
    public ASMNode visit(IRPhi node) throws BaseError {
        throw new OPTError("Phi should be directly translated");
    }

    @Override
    public ASMNode visit(IRStore node) throws BaseError {
        var InstList = new ASMStmt();
        var DestInst = (ASMStmt) node.getDest().accept(this);
        InstList.appendInsts(DestInst);
        var Dest = DestInst.getDest();
        var SrcInst = (ASMStmt) node.getSrc().accept(this);
        InstList.appendInsts(SrcInst);
        var SrcDest = SrcInst.getDest();
        InstList.addInst(new ASMStore(++ASMCounter.InstCount, curBlock, "sw", SrcDest, 0, Dest));
        return InstList;
    }

    @Override
    public ASMNode visit(IREntity node) throws BaseError {
        throw new ASMError("Unknown entity type");
    }

    @Override
    public ASMNode visit(IRVariable node) throws BaseError {
        var InstList = new ASMStmt();
        ASMVirtualReg entity = null;
        if (node.isGlobal()) {
            entity = new ASMVirtualReg(++ASMCounter.allocaCount);
            InstList.addInst(new ASMLoadLabel(++ASMCounter.InstCount, curBlock, entity, node.getValue().substring(1)));
        } else {
            if (!counter.name2reg.containsKey(node.getValue())) {
                entity = new ASMVirtualReg(++ASMCounter.allocaCount);
                counter.name2reg.put(node.getValue(), entity);
                IR2ASM.put(node, entity);
            } else {
                entity = counter.name2reg.get(node.getValue());
            }
        }
        InstList.setDest(entity);
        return InstList;
    }

    @Override
    public ASMNode visit(IRLiteral node) throws BaseError {
        var InstList = new ASMStmt();
        var entity = new ASMVirtualReg(++ASMCounter.allocaCount);
        InstList.addInst(new ASMLi(++ASMCounter.InstCount, curBlock, entity,
                node.getValue().equals("null") ? 0 : Integer.parseInt(node.getValue())));
        InstList.setDest(entity);
        return InstList;
    }
}
