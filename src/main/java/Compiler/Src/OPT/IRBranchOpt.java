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

public class IRBranchOpt {
    private HashMap<IRVariable,Pair<IRBlock,IRInst>> Var2Def;
    private HashMap<IRVariable,Integer> Var2UseNum;
    public void visit(IRRoot root)
    {
        root.getFuncs().forEach(func->work_on_func(func));
    }

    public void work_on_func(IRFuncDef func)
    {
        Collect(func);
        work(func);
    }

    public void work(IRFuncDef func)
    {
        for(var block:func.getBlockstmts())
        {
            if(block.getReturnInst() instanceof IRBranch)
            {
                IRBranch branch = (IRBranch)block.getReturnInst();
                if(branch.isJump())
                {
                    continue;
                }
                if(branch.getCond() instanceof IRLiteral)
                {
                    continue;
                }
                var cond=(IRVariable)branch.getCond();
                if(Var2UseNum.get(cond)==1 && Var2Def.get(cond).b!=null && Var2Def.get(cond).b instanceof IRIcmp)
                {
                    IRIcmp icmp = (IRIcmp)Var2Def.get(cond).b;
                    if(icmp.getCond().equals("eq"))
                    {
                        Var2Def.get(cond).a.getInsts().remove(icmp);
                        block.setReturnInst(new IROptBranch(++InstCounter.InstCounter, icmp.getLhs(), icmp.getRhs(), branch.getTrueLabel(), branch.getFalseLabel(), true));
                    }
                    else if(icmp.getCond().equals("ne"))
                    {
                        Var2Def.get(cond).a.getInsts().remove(icmp);
                        block.setReturnInst(new IROptBranch(++InstCounter.InstCounter, icmp.getLhs(), icmp.getRhs(), branch.getTrueLabel(), branch.getFalseLabel(), false));
                    }
                }
            }
        }
    }

    public void Collect(IRFuncDef func)
    {
        Var2Def = new HashMap<>();
        Var2UseNum = new HashMap<>();
        for(var param:func.getParams())
        {
            Var2Def.put(param,new Pair<IRBlock,IRInst>(null,null));
            Var2UseNum.put(param,0);
        }
        for(var block:func.getBlockstmts())
        {
            for(var phi:block.getPhiList().values())
            {
                Var2Def.put(phi.getDef(),new Pair<IRBlock,IRInst>(block, phi));
                for(var use:phi.getUses())
                {
                    if(!Var2UseNum.containsKey(use))
                        Var2UseNum.put(use,1);
                    else
                        Var2UseNum.put(use,Var2UseNum.get(use)+1);
                }
            }
            for(var inst:block.getInsts())
            {
                if(inst.getDef()!=null)
                {
                    Var2Def.put(inst.getDef(),new Pair<IRBlock,IRInst>(block, inst));
                    for(var use:inst.getUses())
                    {
                        if(!Var2UseNum.containsKey(use))
                            Var2UseNum.put(use,1);
                        else
                            Var2UseNum.put(use,Var2UseNum.get(use)+1);
                    }
                }
            }
            for(var use:block.getReturnInst().getUses())
            {
                if(!Var2UseNum.containsKey(use))
                    Var2UseNum.put(use,1);
                else
                    Var2UseNum.put(use,Var2UseNum.get(use)+1);
            }
        }
    }
}
