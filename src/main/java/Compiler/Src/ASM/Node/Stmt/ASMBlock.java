package Compiler.Src.ASM.Node.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Entity.ASMVirtualReg;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Inst.Control.ASMBranch;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMMove;
import Compiler.Src.ASM.Node.Util.ASMLabel;
import Compiler.Src.ASM.Util.ASMControl;
import Compiler.Src.ASM.Util.ASMCounter;
import Compiler.Src.ASM.Util.BuiltInRegs;
import Compiler.Src.Util.Error.OPTError;

class Node {
    public ASMReg reg;
    public HashSet<Node> next;
    public ASMReg parent;
    public Node(ASMReg register) {
        reg = register;
        next = new HashSet<Node>();
        parent = null;
    }
}

@lombok.Getter
@lombok.Setter
public class ASMBlock extends ASMStmt {
    private ASMLabel label;
    private ASMStmt returnInst;

    public HashSet<ASMReg> uses = null;
    public HashSet<ASMReg> def = null;
    public HashSet<ASMReg> liveIn = null;
    public HashSet<ASMReg> liveOut = null;

    public ArrayList<ASMBlock> pred = null;
    public ArrayList<ASMBlock> succ = null;

    public ASMLabel jlabel;
    public ASMJump jump;

    BuiltInRegs BuiltInRegs;

    // mem2reg
    private ArrayList<String> Successor;
    private ASMStmt PhiStmt;
    private TreeMap<ASMVirtualReg, ArrayList<ASMVirtualReg>> src2dest;
    private int loopDepth;

    public ASMBlock(ASMLabel label) {
        this.label = label;
        this.returnInst = null;
        PhiStmt = new ASMStmt();
        src2dest = new TreeMap<ASMVirtualReg, ArrayList<ASMVirtualReg>>();

        jlabel = null;
        jump = null;

        Successor = new ArrayList<String>();

        uses = new HashSet<ASMReg>();
        def = new HashSet<ASMReg>();
        liveIn = new HashSet<ASMReg>();
        liveOut = new HashSet<ASMReg>();

        pred = new ArrayList<ASMBlock>();
        succ = new ArrayList<ASMBlock>();

        BuiltInRegs = new BuiltInRegs();

        liveIn.add(BuiltInRegs.getSp());
        liveIn.add(BuiltInRegs.getRa());
        liveOut.add(BuiltInRegs.getSp());
        liveOut.add(BuiltInRegs.getRa());
    }

    public void PhiMove_Formal(ASMControl control) {
        // var src2tmp = new TreeMap<>();
        // for (int i = 0; i < src2dest.size(); ++i) {
        //     var src = src2dest.keySet().toArray()[i];
        //     var tmp = new ASMVirtualReg(++ASMCounter.allocaCount);
        //     src2tmp.put(src, tmp);
        //     PhiStmt.addInst(new ASMMove(++ASMCounter.InstCount, this, tmp, (ASMVirtualReg) src));
        // }
        // for (var src : src2dest.keySet()) {
        //     var tmp = (ASMVirtualReg) src2tmp.get(src);
        //     for (var dest : src2dest.get(src)) {
        //         PhiStmt.addInst(new ASMMove(++ASMCounter.InstCount, this, (ASMVirtualReg) dest, tmp));
        //     }
        // }
        HashSet<Node> WorkList = new HashSet<Node>();
        HashMap<ASMReg, Node> reg2node = new HashMap<ASMReg, Node>();
        //initialize
        for(var src:src2dest.keySet()){
            if(!reg2node.containsKey(src)){
                reg2node.put(src, new Node(src));
                WorkList.add(reg2node.get(src));
            }
            for(var dest:src2dest.get(src)){
                if(!reg2node.containsKey(dest)){
                    reg2node.put(dest, new Node(dest));
                    WorkList.add(reg2node.get(dest));
                }
            }
        }

        //build graph
        for(var src:src2dest.keySet()){
            Node srcNode = reg2node.get(src);
            for(var dest:src2dest.get(src)){
                Node destNode = reg2node.get(dest);
                srcNode.next.add(destNode);
                destNode.parent = src;
            }
        }

        //work
        ASMReg mid = new ASMVirtualReg(++ASMCounter.allocaCount);
        while(!WorkList.isEmpty())
        {
            Node node = WorkList.iterator().next();
            WorkList.remove(node);
            LinkedList<Node> queue = new LinkedList<Node>();
            queue.add(node);
            var InstList = new ArrayList<ASMInst>();
            boolean circle = false;
            while(!queue.isEmpty())
            {
                Node cur = queue.poll();
                for(var next:cur.next)
                {
                    if(!next.equals(node))
                    {
                        InstList.add(0,new ASMMove(++ASMCounter.InstCount, this,next.reg,cur.reg));
                        queue.add(next);
                        WorkList.remove(next);
                    }
                    else{
                        InstList.add(0,new ASMMove(++ASMCounter.InstCount, this,mid,cur.reg));
                        circle = true;
                    }
                }
                cur.next.clear();
            }
            if(circle)
            {
                InstList.add(new ASMMove(++ASMCounter.InstCount, this,node.reg,mid));
            }
            PhiStmt.getInsts().addAll(InstList);
        }
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String str = label.toString() + ":\n";
        for (var inst : getInsts()) {
            str += "    " + inst.toString() + "\n";
        }
        if (PhiStmt.getInsts().size() > 0) {
            str += PhiStmt.toString();
        }
        str += returnInst.toString();
        if (jlabel != null) {
            str += jlabel.toString() + ":\n";
            str += "    " + jump.toString() + "\n";
        }
        return str;
    }

    public void replaceLabel(String oldLabel, String newLabel) {
        for (var inst : getReturnInst().getInsts()) {
            if (inst instanceof ASMBranch) {
                if (((ASMBranch) inst).getLabel().equals(oldLabel)) {
                    ((ASMBranch) inst).setLabel(newLabel);
                }
            } else if (inst instanceof ASMJump) {
                if (((ASMJump) inst).getLabel().equals(oldLabel)) {
                    ((ASMJump) inst).setLabel(newLabel);
                }
            } else if (inst instanceof ASMBeq) {
                if (jlabel == null) {
                    throw new OPTError("jlabel is null");
                }
                if (jump.getLabel().equals(oldLabel)) {
                    jump.setLabel(newLabel);
                }
                // if (((ASMBezq) inst).getLabel().equals(oldLabel)) {
                // ((ASMBezq) inst).setLabel(newLabel);
                // }
            }
        }
    }

    public void addSucc(ASMBlock block) {
        if (succ == null) {
            succ = new ArrayList<ASMBlock>();
        }
        succ.add(block);
    }

    public void addPred(ASMBlock block) {
        if (pred == null) {
            pred = new ArrayList<ASMBlock>();
        }
        pred.add(block);
    }
}