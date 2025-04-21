package Compiler.Src.ASM;

import Compiler.Src.ASM.Node.*;
import Compiler.Src.ASM.Node.Global.*;
import Compiler.Src.ASM.Node.Inst.Arithmetic.*;
import Compiler.Src.ASM.Node.Inst.Control.*;
import Compiler.Src.ASM.Node.Inst.Memory.*;
import Compiler.Src.ASM.Node.Inst.Presudo.*;
import Compiler.Src.ASM.Node.Stmt.*;
import Compiler.Src.ASM.Node.Util.ASMLabel;

public interface ASMVisitor<T> {
    public T visit(ASMNode node);

    public T visit(ASMRoot node);

    public T visit(ASMFuncDef node);

    public T visit(ASMBlock node);

    public T visit(ASMStrDef node);

    public T visit(ASMVarDef node);

    public T visit(ASMArithI node);

    public T visit(ASMArithR node);

    public T visit(ASMBranch node);

    public T visit(ASMJump node);

    public T visit(ASMLoad node);

    public T visit(ASMLoadLabel node);

    public T visit(ASMStore node);

    public T visit(ASMBeq node);

    public T visit(ASMLi node);

    public T visit(ASMMove node);

    public T visit(ASMRet node);

    public T visit(ASMUnarry node);

    public T visit(ASMLabel node);
}
