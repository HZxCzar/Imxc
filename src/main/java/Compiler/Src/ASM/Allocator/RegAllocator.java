package Compiler.Src.ASM.Allocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.ASM.Entity.ASMPhysicalReg;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.ASMRoot;
import Compiler.Src.ASM.Node.Global.ASMFuncDef;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Inst.Arithmetic.ASMArithR;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Memory.ASMLoad;
import Compiler.Src.ASM.Node.Inst.Memory.ASMStore;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMCall;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMLi;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMMove;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Node.Stmt.ASMStmt;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.ASM.Util.ASMCounter;
import Compiler.Src.ASM.Util.BuiltInRegs;
import Compiler.Src.Util.Error.OPTError;

public class RegAllocator {
    private static final int K = 27;

    ASMRoot root;
    ASMFuncDef currentFunc;
    HashSet<ASMReg> precolored = new HashSet<>();
    HashSet<ASMReg> initial = new HashSet<>();
    ArrayList<ASMReg> simplifyWorkList = new ArrayList<>();
    ArrayList<ASMReg> freezeWorkList = new ArrayList<>();
    ArrayList<ASMReg> spillWorkList = new ArrayList<>();
    HashSet<ASMReg> spilledNodes = new HashSet<>();
    HashSet<ASMReg> coalescedNodes = new HashSet<>();
    HashSet<ASMReg> coloredNodes = new HashSet<>();
    Stack<ASMReg> selectStack = new Stack<>();

    HashSet<ASMMove> coalescedMoves = new HashSet<>();
    HashSet<ASMMove> constrainedMoves = new HashSet<>();
    HashSet<ASMMove> frozenMoves = new HashSet<>();
    HashSet<ASMMove> workListMoves = new HashSet<>();
    HashSet<ASMMove> activeMoves = new HashSet<>();

    HashSet<Pair<ASMReg, ASMReg>> adjSet = new HashSet<>();
    HashMap<ASMReg, HashSet<ASMReg>> adjList = new HashMap<>();
    HashMap<ASMReg, Integer> degree = new HashMap<>();
    HashMap<ASMReg, HashSet<ASMMove>> moveList = new HashMap<>();
    HashMap<ASMReg, ASMReg> alias = new HashMap<>();
    HashMap<ASMReg, Integer> color = new HashMap<>();

    // tool
    HashMap<ASMReg, Integer> regMap = new HashMap<>();
    HashSet<ASMReg> spillTemp = new HashSet<>();
    BuiltInRegs BuiltInRegs;

    HashMap<ASMReg, Double> regDepth = new HashMap<>();

    public RegAllocator(ASMRoot root) {
        this.root = root;
        BuiltInRegs = new BuiltInRegs();
    }

    public void Main() {
        for (var func : root.getFuncs()) {
            currentFunc = func;
            spillTemp.clear();
            Run(func);
            for (var block : func.getBlocks()) {
                var newInsts = new ArrayList<ASMInst>();
                for (var inst : block.getInsts()) {
                    if (inst.getDef() != null) {
                        inst.setDest(getColor(inst.getDef()));
                    }
                    for (var reg : inst.getUses()) {
                        inst.replaceUse(reg, getColor(reg));
                    }
                    if (!(inst instanceof ASMMove) || !inst.getDef().equals(inst.getUses().get(0))) {
                        newInsts.add(inst);
                    }
                }
                block.setInsts(newInsts);
                var newPhiInsts = new ArrayList<ASMInst>();
                for (var inst : block.getPhiStmt().getInsts()) {
                    if (inst.getDef() != null) {
                        inst.setDest(getColor(inst.getDef()));
                    }
                    for (var reg : inst.getUses()) {
                        inst.replaceUse(reg, getColor(reg));
                    }
                    if (!(inst instanceof ASMMove) || !inst.getDef().equals(inst.getUses().get(0))) {
                        newPhiInsts.add(inst);
                    }
                }
                block.getPhiStmt().setInsts(newPhiInsts);
                var newReturnInsts = new ArrayList<ASMInst>();
                for (var inst : block.getReturnInst().getInsts()) {
                    if (inst.getDef() != null) {
                        inst.setDest(getColor(inst.getDef()));
                    }
                    for (var reg : inst.getUses()) {
                        inst.replaceUse(reg, getColor(reg));
                    }
                    if (!(inst instanceof ASMMove) || !inst.getDef().equals(inst.getUses().get(0))) {
                        newReturnInsts.add(inst);
                    }
                }
                block.getReturnInst().setInsts(newReturnInsts);

                // var newLiveOut=new HashSet<ASMReg>();
                // for (var reg : block.getLiveOut()) {
                // newLiveOut.add(getColor(reg));
                // }
                // block.setLiveOut(newLiveOut);
            }
            // new LiveAnalysis().LiveAnalysisFinal(func);
            func.setColor(new HashMap<ASMReg, Integer>(color));
        }
    }

    public void Run(ASMFuncDef func) {
        // currentFunc = func;
        // spillTemp.clear();
        // new LiveAnalysis().LiveAnalysisMethod(func);
        init();
        Build(func);
        MakeWorkList();
        do {
            if (!simplifyWorkList.isEmpty()) {
                Simplify();
            } else if (!workListMoves.isEmpty()) {
                Coalesce();
            } else if (!freezeWorkList.isEmpty()) {
                Freeze();
            } else if (!spillWorkList.isEmpty()) {
                SelectSpill();
            }
        } while (!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty()
                || !spillWorkList.isEmpty());
        AssignColors();
        if (!spilledNodes.isEmpty()) {
            RewriteProgram();
            ChangeLiveOut();
            Run(func);
        }
    }

    public void ChangeLiveOut() {
        for (var block : currentFunc.getBlocks()) {
            // HashSet<ASMReg> deadLiveOut = new HashSet<>();
            // for (var reg : block.getLiveOut()) {
            //     if (spilledNodes.contains(reg)) {
            //         deadLiveOut.add(reg);
            //     }
            // }
            // for (var reg : deadLiveOut) {
            //     block.getLiveOut().remove(reg);
            // }
            block.getLiveOut().removeAll(spilledNodes);
        }
    }

    public void init() {
        precolored.clear();
        initial.clear();
        simplifyWorkList.clear();
        freezeWorkList.clear();
        spillWorkList.clear();
        spilledNodes.clear();
        coalescedNodes.clear();
        coloredNodes.clear();
        selectStack.clear();

        coalescedMoves.clear();
        constrainedMoves.clear();
        frozenMoves.clear();
        workListMoves.clear();
        activeMoves.clear();

        adjSet.clear();
        adjList.clear();
        degree.clear();
        moveList.clear();
        alias.clear();
        color.clear();

        regMap.clear();
        regDepth.clear();
        for (var block : currentFunc.getBlocks()) {
            for (var inst : block.getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    regMap.put(inst.getDef(), regMap.getOrDefault(inst.getDef(), 0) + 1);
                    moveList.put(inst.getDef(), new HashSet<>());
                    var reg = inst.getDef();
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                    // }
                }
            }
            for (var inst : block.getPhiStmt().getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    regMap.put(inst.getDef(), regMap.getOrDefault(inst.getDef(), 0) + 1);
                    moveList.put(inst.getDef(), new HashSet<>());
                    var reg = inst.getDef();
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                    // }
                }
            }
            for (var inst : block.getReturnInst().getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    regMap.put(inst.getDef(), regMap.getOrDefault(inst.getDef(), 0) + 1);
                    moveList.put(inst.getDef(), new HashSet<>());
                    var reg = inst.getDef();
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    regMap.put(reg, regMap.getOrDefault(reg, 0) + 1);
                    moveList.put(reg, new HashSet<>());
                    if (reg instanceof ASMPhysicalReg) {
                        precolored.add(reg);
                        color.put(reg, ((ASMPhysicalReg) reg).getColor());
                        degree.put(reg, Integer.MAX_VALUE);
                    } else {
                        initial.add(reg);
                        adjList.put(reg, new HashSet<>());
                        degree.put(reg, 0);
                        // regDepth.put(reg, (double) 0);
                    }
                    // }
                }
            }
        }

        // spill weight
        for (var block : currentFunc.getBlocks()) {
            double weight = Math.pow(10, block.getLoopDepth());
            for (var inst : block.getPhiStmt().getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    var reg = inst.getDef();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                for (var reg : inst.getUses()) {
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
            }
            for (var inst : block.getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    var reg = inst.getDef();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                for (var reg : inst.getUses()) {
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
            }
            for (var inst : block.getReturnInst().getInsts()) {
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    var reg = BuiltInRegs.getA0();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                if (inst.getDef() != null) {// && !FixedReg(inst.getDef())
                    var reg = inst.getDef();
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
                for (var reg : inst.getUses()) {
                    if (!(reg instanceof ASMPhysicalReg)) {
                        regDepth.put(reg, regDepth.getOrDefault(reg, (double) 0) + weight);
                    }
                }
            }
        }
    }

    // public boolean CallRelated(ASMReg reg) {
    //     if (reg instanceof ASMPhysicalReg) {
    //         if (((ASMPhysicalReg) reg).equals(BuiltInRegs.getSp())
    //                 || ((ASMPhysicalReg) reg).equals(BuiltInRegs.getRa())
    //                 || ((ASMPhysicalReg) reg).equals(BuiltInRegs.getT1())) {
    //             return false;
    //         }
    //         return true;
    //     }
    //     return false;
    // }

    public void Build(ASMFuncDef func) {
        for (var block : func.getBlocks()) {
            var live = new HashSet<ASMReg>(block.getLiveOut());
            for (int i = block.getReturnInst().getInsts().size() - 1; i >= 0; --i) {
                var inst = block.getReturnInst().getInsts().get(i);
                if (inst instanceof ASMMove) {
                    // live.remove(inst.getDef());
                    moveList.get(inst.getDef()).add((ASMMove) inst);
                    moveList.get(inst.getUses().get(0)).add((ASMMove) inst);
                    workListMoves.add((ASMMove) inst);
                }
                // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
                //     if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
                //         continue;
                //     }
                //     if (inst instanceof ASMStore && CallRelated(((ASMStore) inst).getUses().get(1))) {
                //         continue;
                //     }
                // }
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    for (var l : live) {
                        AddEdge(l, BuiltInRegs.getA0());
                    }
                    live.remove(BuiltInRegs.getA0());
                }
                if (inst.getDef() != null) { // && !FixedReg(inst.getDef())
                    // live.add(inst.getDef());
                    for (var l : live) {
                        AddEdge(l, inst.getDef());
                    }
                    live.remove(inst.getDef());
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    live.add(reg);
                    // }
                }
            }
            for (int i = block.getPhiStmt().getInsts().size() - 1; i >= 0; --i) {
                var inst = block.getPhiStmt().getInsts().get(i);
                if (inst instanceof ASMMove) {
                    // live.removeAll(inst.getUses());
                    moveList.get(inst.getDef()).add((ASMMove) inst);
                    moveList.get(inst.getUses().get(0)).add((ASMMove) inst);
                    workListMoves.add((ASMMove) inst);
                }
                // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
                //     if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
                //         continue;
                //     }
                //     if (inst instanceof ASMStore && CallRelated(((ASMStore) inst).getUses().get(1))) {
                //         continue;
                //     }
                // }
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    for (var l : live) {
                        AddEdge(l, BuiltInRegs.getA0());
                    }
                    live.remove(BuiltInRegs.getA0());
                }
                if (inst.getDef() != null) { // && !FixedReg(inst.getDef())
                    // live.add(inst.getDef());
                    for (var l : live) {
                        AddEdge(l, inst.getDef());
                    }
                    live.remove(inst.getDef());
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    live.add(reg);
                    // }
                }
            }
            for (int i = block.getInsts().size() - 1; i >= 0; --i) {
                var inst = block.getInsts().get(i);
                if (inst instanceof ASMMove) {
                    // live.remove(inst.getDef());
                    moveList.get(inst.getDef()).add((ASMMove) inst);
                    moveList.get(inst.getUses().get(0)).add((ASMMove) inst);
                    workListMoves.add((ASMMove) inst);
                }
                // if (inst instanceof ASMLoad || inst instanceof ASMStore) {
                //     if (inst instanceof ASMLoad && CallRelated(((ASMLoad) inst).getDef())) {
                //         continue;
                //     }
                //     if (inst instanceof ASMStore && CallRelated(((ASMStore) inst).getUses().get(1))) {
                //         continue;
                //     }
                // }
                if (inst instanceof ASMCall && ((ASMCall) inst).isHasReturnValue()) {
                    for (var l : live) {
                        AddEdge(l, BuiltInRegs.getA0());
                    }
                    live.remove(BuiltInRegs.getA0());
                }
                if (inst.getDef() != null) { // && !FixedReg(inst.getDef())
                    // live.add(inst.getDef());
                    for (var l : live) {
                        AddEdge(l, inst.getDef());
                    }
                    live.remove(inst.getDef());
                }
                for (var reg : inst.getUses()) {
                    // if (!FixedReg(reg)) {
                    live.add(reg);
                    // }
                }
            }
        }
    }

    public void AddEdge(ASMReg u, ASMReg v) {
        if (adjSet.contains(new Pair<>(u, v)) || u == v) {
            return;
        }
        adjSet.add(new Pair<>(u, v));
        adjSet.add(new Pair<>(v, u));
        if (!precolored.contains(u)) {
            if (u instanceof ASMPhysicalReg)
                throw new OPTError("u is ASMPhysicalReg");
            adjList.get(u).add(v);
            degree.put(u, degree.get(u) + 1);
        }
        if (!precolored.contains(v)) {
            if (v instanceof ASMPhysicalReg)
                throw new OPTError("v is ASMPhysicalReg");
            adjList.get(v).add(u);
            degree.put(v, degree.get(v) + 1);
        }
    }

    public void MakeWorkList() {
        for (var reg : initial) {
            if (degree.get(reg) >= K) {
                spillWorkList.add(reg);
            } else if (MoveRelated(reg)) {
                freezeWorkList.add(reg);
            } else {
                simplifyWorkList.add(reg);
            }
        }
        initial.clear();
    }

    public HashSet<ASMReg> Adjacent(ASMReg reg) {
        HashSet<ASMReg> ret = new HashSet<>(adjList.get(reg));
        ret.removeIf(r -> coalescedNodes.contains(r) || selectStack.contains(r));
        return ret;
    }

    public HashSet<ASMMove> NodeMoves(ASMReg reg) {
        HashSet<ASMMove> ret = moveList.get(reg);
        ret.removeIf(mv -> !activeMoves.contains(mv) && !workListMoves.contains(mv));
        return ret;
    }

    public boolean MoveRelated(ASMReg reg) {
        return !NodeMoves(reg).isEmpty();
    }

    public void Simplify() {
        var reg = simplifyWorkList.get(0);
        simplifyWorkList.remove(0);
        selectStack.push(reg);
        for (var m : Adjacent(reg)) {
            DecrementDegree(m);
        }
    }

    public void DecrementDegree(ASMReg m) {
        var d = degree.get(m);
        degree.put(m, d - 1);
        if (d == K) {
            var nodes = new HashSet<>(Adjacent(m));
            nodes.add(m);
            EnableMoves(nodes);
            spillWorkList.remove(m);
            if (MoveRelated(m)) {
                freezeWorkList.add(m);
            } else {
                simplifyWorkList.add(m);
            }
        }
    }

    public void EnableMoves(HashSet<ASMReg> nodes) {
        for (var n : nodes) {
            for (var m : NodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }

    public boolean OK(ASMReg t, ASMReg r) {
        return degree.get(t) < K || precolored.contains(t) || adjSet.contains(new Pair<>(t, r));
    }

    public boolean Conservative(HashSet<ASMReg> nodes) {
        int k = 0;
        for (var n : nodes) {
            if (degree.get(n) >= K) {
                k++;
            }
        }
        return k < K;
    }

    public ASMReg GetAlias(ASMReg n) {
        if (coalescedNodes.contains(n)) {
            return GetAlias(alias.get(n));
        } else {
            return n;
        }
    }

    public void Coalesce() {
        var m = workListMoves.iterator().next();
        workListMoves.remove(m);
        var x = GetAlias(m.getDef());
        var y = GetAlias(m.getUses().get(0));
        Pair<ASMReg, ASMReg> pair;
        if (precolored.contains(y)) {
            pair = new Pair<>(y, x);
        } else {
            pair = new Pair<>(x, y);
        }
        if (pair.a == pair.b) {
            coalescedMoves.add(m);
            AddWorkList(pair.a);
            return;
        } else if (precolored.contains(pair.b) || adjSet.contains(pair)) {
            constrainedMoves.add(m);
            AddWorkList(pair.a);
            AddWorkList(pair.b);
            return;
        } else if ((precolored.contains(pair.a) && Adjacent(pair.b).stream().allMatch(t -> OK(t, pair.a)))
                || (!precolored.contains(pair.a) && Conservative(new HashSet<ASMReg>() {
                    {
                        addAll(Adjacent(pair.a));
                        addAll(Adjacent(pair.b));
                    }
                }))) {
            coalescedMoves.add(m);
            Combine(pair.a, pair.b);
            AddWorkList(pair.a);
        } else {
            activeMoves.add(m);
        }
    }

    public void AddWorkList(ASMReg u) {
        if (!precolored.contains(u) && !MoveRelated(u) && degree.get(u) < K) {
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    public void Combine(ASMReg u, ASMReg v) {
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v);
        } else {
            spillWorkList.remove(v);
        }
        coalescedNodes.add(v);
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));
        HashSet<ASMReg> vSet = new HashSet<>();
        vSet.add(v);
        EnableMoves(vSet);
        for (var t : Adjacent(v)) {
            AddEdge(t, u);
            DecrementDegree(t);
        }
        if (degree.get(u) >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    public void Freeze() {
        // for (var u : freezeWorkList) {
        // freezeWorkList.remove(u);
        var u = freezeWorkList.remove(freezeWorkList.size() - 1);
        simplifyWorkList.add(u);
        FreezeMoves(u);
        // }
    }

    public void FreezeMoves(ASMReg u) {
        for (var m : NodeMoves(u)) {
            var x = m.getDef();
            var y = m.getUses().get(0);
            ASMReg v;
            if (GetAlias(y) == GetAlias(u)) {
                v = GetAlias(x);
            } else {
                v = GetAlias(y);
            }
            activeMoves.remove(m);
            frozenMoves.add(m);
            if (NodeMoves(v).isEmpty() && degree.get(v) < K) {
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
    }

    public void SelectSpill() {
        ASMReg m = null;
        for (var reg : spillWorkList) {
            // if (m == null
            // || regMap.get(reg) / degree.get(reg) < regMap.get(m) / degree.get(m) &&
            // !spillTemp.contains(reg)) {
            // m = reg;
            // }
            if ((m == null || regDepth.get(reg) / degree.get(reg) < regDepth.get(m) / degree.get(m))
                    && !spillTemp.contains(reg) && reg instanceof ASMVirtualReg) {
                m = reg;
            }
        }
        if (m == null) {
            throw new OPTError("No spill node found");
        }
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        FreezeMoves(m);
    }

    public void AssignColors() {
        // int a=1;
        while (!selectStack.isEmpty()) {
            var n = selectStack.pop();
            HashSet<Integer> okColors = new HashSet<>();
            for (int i = 0; i < K; ++i) {
                okColors.add(i + 5);
            }
            for (var w : adjList.get(n)) {
                var aw = GetAlias(w);
                if (coloredNodes.contains(aw) || precolored.contains(aw)) {
                    okColors.remove(color.get(aw));
                }
            }
            if (okColors.isEmpty()) {
                spilledNodes.add(n);
            } else {
                coloredNodes.add(n);
                var c = okColors.iterator().next();
                color.put(n, c);
            }
        }
        for (var n : coalescedNodes) {
            color.put(n, color.get(GetAlias(n)));
        }
    }

    public ASMPhysicalReg getColor(ASMReg reg) {
        if (reg instanceof ASMPhysicalReg) {
            return (ASMPhysicalReg) reg;
        } else {
            if (color.get(reg) == null) {
                return null;
            }
            int imm = color.get(reg);
            return BuiltInRegs.get(imm);
        }
    }

    public void RewriteProgram() {
        HashMap<ASMReg, Integer> newRegs = new HashMap<>();
        for (var reg : spilledNodes) {
            newRegs.put(reg, currentFunc.getStackSize());
            currentFunc.setStackSize(currentFunc.getStackSize() + 4);
        }

        for (var block : currentFunc.getBlocks()) {
            for (int i = 0; i < block.getInsts().size(); ++i) {
                var inst = block.getInsts().get(i);
                if (inst.getDef() != null && spilledNodes.contains(inst.getDef())) {
                    // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmp = BuiltInRegs.getT0();
                    var imm = newRegs.get(inst.getDef());
                    inst.setDest(tmp);
                    spillTemp.add(tmp);
                    if (imm < -2048 || imm > 2047) {
                        // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        block.addInst(i + 1, new ASMLi(++ASMCounter.InstCount, block, BuiltInRegs.getT1(), imm));
                        block.addInst(i + 2,
                                new ASMArithR(++ASMCounter.InstCount, block, "add", BuiltInRegs.getT1(),
                                        BuiltInRegs.getSp(),
                                        BuiltInRegs.getT1()));
                        block.addInst(i + 3,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, 0, BuiltInRegs.getT1()));
                    } else {
                        block.addInst(i + 1,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, imm, BuiltInRegs.getSp()));
                    }
                }
                var useSize = inst.getUses().size();
                for (int j = 0; j < useSize; ++j) {
                    var reg = inst.getUses().get(j);
                    if (spilledNodes.contains(reg)) {
                        // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        var tmp=j==0?BuiltInRegs.getT0():BuiltInRegs.getT1();
                        var imm = newRegs.get(reg);
                        inst.replaceUse(reg, tmp);
                        spillTemp.add(tmp);
                        if (imm < -2048 || imm > 2047) {
                            // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                            block.addInst(i, new ASMLi(++ASMCounter.InstCount, block, tmp, imm));
                            block.addInst(i + 1,
                                    new ASMArithR(++ASMCounter.InstCount, block, "add", tmp, BuiltInRegs.getSp(),
                                            tmp));
                            block.addInst(i + 2,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, 0,
                                            tmp));
                        } else {
                            block.addInst(i,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, imm, BuiltInRegs.getSp()));
                        }
                    }
                }
            }

            for (int i = 0; i < block.getPhiStmt().getInsts().size(); ++i) {
                var inst = block.getPhiStmt().getInsts().get(i);
                if (inst.getDef() != null && spilledNodes.contains(inst.getDef())) {// 有无precolored？
                    // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmp = BuiltInRegs.getT0();
                    var imm = newRegs.get(inst.getDef());
                    inst.setDest(tmp);
                    spillTemp.add(tmp);
                    if (imm < -2048 || imm > 2047) {
                        // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        block.getPhiStmt().addInst(i + 1, new ASMLi(++ASMCounter.InstCount, block,
                                BuiltInRegs.getT1(), imm));
                        block.getPhiStmt().addInst(i + 2,
                                new ASMArithR(++ASMCounter.InstCount, block, "add", BuiltInRegs.getT1(),
                                        BuiltInRegs.getSp(),
                                        BuiltInRegs.getT1()));
                        block.getPhiStmt().addInst(i + 3,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, 0,
                                        BuiltInRegs.getT1()));
                    } else {
                        block.getPhiStmt().addInst(i + 1,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, imm, BuiltInRegs.getSp()));
                    }
                }
                var useSize = inst.getUses().size();
                for (int j = 0; j < useSize; ++j) {
                    var reg = inst.getUses().get(j);
                    if (spilledNodes.contains(reg)) {
                        // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        var tmp=j==0?BuiltInRegs.getT0():BuiltInRegs.getT1();
                        var imm = newRegs.get(reg);
                        inst.replaceUse(reg, tmp);
                        spillTemp.add(tmp);
                        if (imm < -2048 || imm > 2047) {
                            // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                            block.getPhiStmt().addInst(i, new ASMLi(++ASMCounter.InstCount, block,
                                    tmp, imm));
                            block.getPhiStmt().addInst(i + 1,
                                    new ASMArithR(++ASMCounter.InstCount, block, "add", tmp, BuiltInRegs.getSp(),
                                            tmp));
                            block.getPhiStmt().addInst(i + 2,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, 0,
                                            tmp));
                        } else {
                            block.getPhiStmt().addInst(i,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, imm, BuiltInRegs.getSp()));
                        }
                    }
                }
            }

            for (int i = 0; i < block.getReturnInst().getInsts().size(); ++i) {
                var inst = block.getReturnInst().getInsts().get(i);
                if (inst.getDef() != null && spilledNodes.contains(inst.getDef())) {
                    // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                    var tmp = BuiltInRegs.getT0();
                    var imm = newRegs.get(inst.getDef());
                    inst.setDest(tmp);
                    spillTemp.add(tmp);
                    if (imm < -2048 || imm > 2047) {
                        // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        block.getReturnInst().addInst(i + 1, new ASMLi(++ASMCounter.InstCount, block,
                                BuiltInRegs.getT1(), imm));
                        block.getReturnInst().addInst(i + 2,
                                new ASMArithR(++ASMCounter.InstCount, block, "add", BuiltInRegs.getT1(),
                                        BuiltInRegs.getSp(),
                                        BuiltInRegs.getT1()));
                        block.getReturnInst().addInst(i + 3,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, 0,
                                        BuiltInRegs.getT1()));
                    } else {
                        block.getReturnInst().addInst(i + 1,
                                new ASMStore(++ASMCounter.InstCount, block, "sw", tmp, imm, BuiltInRegs.getSp()));
                    }
                }
                var useSize = inst.getUses().size();
                for (int j = 0; j < useSize; ++j) {
                    var reg = inst.getUses().get(j);
                    if (spilledNodes.contains(reg)) {
                        // var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                        var tmp=j==0?BuiltInRegs.getT0():BuiltInRegs.getT1();
                        var imm = newRegs.get(reg);
                        inst.replaceUse(reg, tmp);
                        spillTemp.add(tmp);
                        if (imm < -2048 || imm > 2047) {
                            // var immtmp = new ASMVirtualReg(++ASMCounter.allocaCount);
                            block.getReturnInst().addInst(i, new ASMLi(++ASMCounter.InstCount, block,
                                    tmp, imm));
                            block.getReturnInst().addInst(i + 1,
                                    new ASMArithR(++ASMCounter.InstCount, block, "add", tmp, BuiltInRegs.getSp(),
                                            tmp));
                            block.getReturnInst().addInst(i + 2,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, 0,
                                            tmp));
                        } else {
                            block.getReturnInst().addInst(i,
                                    new ASMLoad(++ASMCounter.InstCount, block, "lw", tmp, imm, BuiltInRegs.getSp()));
                        }
                    }
                }
            }
        }
    }
}
