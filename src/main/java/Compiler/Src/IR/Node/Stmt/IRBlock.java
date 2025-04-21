package Compiler.Src.IR.Node.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import javax.print.DocFlavor.STRING;

import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.Inst.IRInst;
import Compiler.Src.IR.Node.Inst.IRPhi;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRBlock extends IRStmt implements Comparable<IRBlock> {
    private IRLabel labelName;

    private IRInst returnInst;

    // CFG
    private HashSet<IRBlock> successors;
    private HashSet<IRBlock> predecessors;

    // Mem2Reg
    private IRBlock idom;
    private IRBlock Ridom;
    private HashSet<IRBlock> DomFrontier;
    private HashSet<IRBlock> RDomFrontier;
    private HashSet<IRBlock> DomChildren;
    private HashSet<IRBlock> RDomChildren;

    // LiveAnalysis
    public HashSet<IRVariable> liveIn;
    public HashMap<IRLabel,HashSet<IRVariable>> liveInPhi;
    public HashSet<IRVariable> liveOut;
    public HashSet<IRVariable> uses = null;
    public HashSet<IRVariable> def = null;
    public HashMap<IRLabel,HashSet<IRVariable>> usesPhi = null;
    public HashMap<IRLabel,HashSet<IRVariable>> defPhi = null;

    private HashMap<IRVariable, IRPhi> PhiList;

    private int loopDepth;

    public IRBlock(IRLabel labelName, int loopDepth) {
        this.labelName = labelName;
        this.returnInst = null;

        this.loopDepth = loopDepth;

        // CFG
        this.successors = new HashSet<IRBlock>();
        this.predecessors = new HashSet<IRBlock>();

        // Mem2Reg
        this.idom = null;
        this.Ridom = null;
        this.DomFrontier = new HashSet<IRBlock>();
        this.RDomFrontier = new HashSet<IRBlock>();
        this.DomChildren = new HashSet<IRBlock>();
        this.RDomChildren = new HashSet<IRBlock>();

        this.PhiList = new HashMap<IRVariable, IRPhi>();

        // LiveAnalysis
        this.liveIn = new HashSet<IRVariable>();
        this.liveInPhi = new HashMap<IRLabel,HashSet<IRVariable>>();
        this.liveOut = new HashSet<IRVariable>();
        this.uses = new HashSet<IRVariable>();
        this.def = new HashSet<IRVariable>();
        this.usesPhi = new HashMap<IRLabel,HashSet<IRVariable>>();
        this.defPhi = new HashMap<IRLabel,HashSet<IRVariable>>();
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String str = labelName.toString() + ":\n";
        for (var phi : PhiList.values()) {
            str += "  " + phi.toString() + "\n";
        }
        for (var inst : getInsts()) {
            str += "  " + inst.toString() + "\n";
        }
        str += "  " + returnInst.toString() + "\n";
        return str;
    }

    // CFG

    public void addSuccessor(IRBlock block) {
        this.successors.add(block);
    }

    public void addPredecessor(IRBlock block) {
        this.predecessors.add(block);
    }

    public void RemoveInst(IRInst inst) {
        if (inst instanceof IRPhi) {
            for (var var : getPhiList().entrySet()) {
                if (var.getValue().equals(inst)) {
                    getPhiList().remove(var.getKey());
                    break;
                }
            }
        }
        getInsts().remove(inst);
    }

    @Override
    public int compareTo(IRBlock rhs) {
        return labelName.compareTo(((IRBlock) rhs).getLabelName());
    }

    @Override
    public int hashCode() {
        return labelName.hashCode();
    }
}
