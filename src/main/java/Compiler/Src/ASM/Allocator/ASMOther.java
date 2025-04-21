package Compiler.Src.ASM.Allocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Compiler.Src.ASM.Entity.ASMPhysicalReg;
import Compiler.Src.ASM.Entity.ASMReg;
import Compiler.Src.ASM.Node.ASMRoot;
import Compiler.Src.ASM.Node.Global.ASMFuncDef;
import Compiler.Src.ASM.Node.Inst.ASMInst;
import Compiler.Src.ASM.Node.Inst.Control.ASMJump;
import Compiler.Src.ASM.Node.Inst.Memory.ASMLoad;
import Compiler.Src.ASM.Node.Inst.Memory.ASMStore;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMBeq;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMCall;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMLi;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMMove;
import Compiler.Src.ASM.Node.Inst.Presudo.ASMRet;
import Compiler.Src.ASM.Node.Stmt.ASMBlock;
import Compiler.Src.ASM.Node.Stmt.ASMStmt;
import Compiler.Src.ASM.Util.ASMCounter;
import Compiler.Src.ASM.Util.BuiltInRegs;
import Compiler.Src.Util.Error.OPTError;
public class ASMOther {
    HashMap<String,Integer> name2block;
    public void visit(ASMRoot root)
    {
        name2block=new HashMap<>();
        root.getFuncs().forEach(func->J2B(func));
        root.getFuncs().forEach(func->Jmove(func));
    }

    private int computedist(ASMFuncDef func,int a,int b)
    {
        int dist=0;
        int beg=a>b?b:a;
        int end=a>b?a:b;
        for(int i=beg;i<=end;++i)
        {
            var block=func.getBlocks().get(i);
            dist+=block.getInsts().size();
            dist+=block.getPhiStmt().getInsts().size();
            dist+=block.getReturnInst().getInsts().size();
        }
        return dist;
    }

    public void J2B(ASMFuncDef func)
    {
        for(int i=0;i<func.getBlocks().size();++i)
        {
            var block=func.getBlocks().get(i);
            name2block.put(block.getLabel().getLabel(),i);
        }
        for(int i=0;i<func.getBlocks().size();++i)
        {
            var block=func.getBlocks().get(i);
            for(var ret:block.getReturnInst().getInsts())
            {
                if(ret instanceof ASMBeq)
                {
                    if(computedist(func,name2block.get(block.jump.getLabel()),i)<800)
                    {
                        if(block.jlabel==null)
                        {
                            throw new OPTError("J2B error");
                        }
                        ((ASMBeq)ret).setLabel(block.jump.getLabel());
                        block.setJlabel(null);
                        block.setJump(null);
                    }
                }
            }
        }
    }

    public void Jmove(ASMFuncDef func)
    {
        for(int i=0;i<func.getBlocks().size();++i)
        {
            var block=func.getBlocks().get(i);
            for(int ind=0;ind<block.getInsts().size();++ind)
            {
                var inst=block.getInsts().get(ind);
                if(inst instanceof ASMMove && ((ASMMove)inst).getDest().equals(((ASMMove)inst).getRs1()))
                {
                    block.getInsts().remove(inst);
                    --ind;
                }
            }
            for(var ind=0;ind<block.getPhiStmt().getInsts().size();++ind)
            {
                var inst=block.getPhiStmt().getInsts().get(ind);
                if(inst instanceof ASMMove && ((ASMMove)inst).getDest().equals(((ASMMove)inst).getRs1()))
                {
                    block.getPhiStmt().getInsts().remove(inst);
                    --ind;
                }
            }       
            if(block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-1) instanceof ASMJump)
            {
                var inst = (ASMJump)block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-1);
                if(block.jlabel==null && i+1<func.getBlocks().size() && func.getBlocks().get(i+1).getLabel().getLabel().equals(inst.getLabel()))
                {
                    block.getReturnInst().getInsts().remove(block.getReturnInst().getInsts().size()-1);
                }
                else if(block.jlabel!=null && i+1<func.getBlocks().size())
                {
                    var jump=block.getJump();
                    if(func.getBlocks().get(i+1).getLabel().getLabel().equals(jump.getLabel()) && block.getReturnInst().getInsts().size()>=2)
                    {
                        var beqInst=(ASMBeq)block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-2);
                        beqInst.setLabel(jump.getLabel());
                        block.setJlabel(null);
                        block.setJump(null);
                    }
                }
                if(block.jlabel==null && i+1<func.getBlocks().size())
                {
                    if(block.getReturnInst().getInsts().size()>=2 && block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-2) instanceof ASMBeq)
                    {
                        var beqInst=(ASMBeq)block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-2);
                        if(beqInst.getType()!=0 && func.getBlocks().get(i+1).getLabel().getLabel().equals(beqInst.getLabel()) && computedist(func,name2block.get(((ASMJump)block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-1)).getLabel()),i)<1000)
                        {
                            beqInst.setLabel(((ASMJump)block.getReturnInst().getInsts().get(block.getReturnInst().getInsts().size()-1)).getLabel());
                            block.getReturnInst().getInsts().remove(block.getReturnInst().getInsts().size()-1);
                            if(beqInst.getType()==1)
                            {
                                beqInst.setType(2);
                            }
                            else{
                                beqInst.setType(1);
                            }
                        }
                    }
                }
            }
        }
    }
}
