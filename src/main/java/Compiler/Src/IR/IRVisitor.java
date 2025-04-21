package Compiler.Src.IR;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.ASM.Node.ASMNode;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.IR.Node.*;
import Compiler.Src.IR.Node.Def.*;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.*;

public interface IRVisitor<T> {
    public T visit(IRNode node) throws BaseError;
    public T visit(IRRoot node) throws BaseError;
    
    public T visit(IRFuncDef node) throws BaseError;
    public T visit(IRBlock node) throws BaseError;
    public T visit(IRGlobalDef node) throws BaseError;
    public T visit(IRStrDef node) throws BaseError;
  
    public T visit(IRAlloca node) throws BaseError;
    public T visit(IRArith node) throws BaseError;
    public T visit(IRBranch node) throws BaseError;
    public T visit(IRCall node) throws BaseError;
    public T visit(IRGetelementptr node) throws BaseError;
    public T visit(IRRet node) throws BaseError;
    public T visit(IRLoad node) throws BaseError;
    public T visit(IRPhi node) throws BaseError;
    public T visit(IRIcmp node) throws BaseError;
    // public T visit(IRJump node) throws BaseError;
    public T visit(IRStore node) throws BaseError;
    public T visit(IROptBranch node) throws BaseError;
    // public T visit(IRCommentNode node) throws BaseError;
    // public T visit(IRCustomNode node) throws BaseError;
    // public T visit(IRLabelNode node) throws BaseError;
    
    public T visit(IREntity node) throws BaseError;
    public T visit(IRVariable node) throws BaseError;
    public T visit(IRLiteral node) throws BaseError;
  }
  
