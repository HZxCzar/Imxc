package Compiler.Src.AST;

import Compiler.Src.AST.Node.*;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;
import Compiler.Src.AST.Node.ExprNode.*;
import Compiler.Src.AST.Node.StatementNode.*;
import Compiler.Src.Util.Error.*;


public interface ASTVisitor<T> {
    public T visit(ASTNode node) throws BaseError;
    public T visit(ASTRoot node) throws BaseError;

    public T visit(ASTFuncDef node) throws BaseError;
    public T visit(ASTClassDef node) throws BaseError;
    public T visit(ASTVarDef node) throws BaseError;

    public T visit(ASTConstarray node) throws BaseError;
    public T visit(ASTFstring node) throws BaseError;
    public T visit(ASTNewExpr node) throws BaseError;
    public T visit(ASTMemberExpr node) throws BaseError;
    public T visit(ASTCallExpr node) throws BaseError;
    public T visit(ASTArrayExpr node) throws BaseError;
    public T visit(ASTUnaryExpr node) throws BaseError;
    public T visit(ASTPreunaryExpr node) throws BaseError;
    public T visit(ASTBinaryExpr node) throws BaseError;
    public T visit(ASTConditionalExpr node) throws BaseError;
    public T visit(ASTAssignExpr node) throws BaseError;
    public T visit(ASTAtomExpr node) throws BaseError;
    public T visit(ASTParenExpr node) throws BaseError;

    public T visit(ASTBlockstatement node) throws BaseError;
    public T visit(ASTBreakstatement node) throws BaseError;
    public T visit(ASTContinuestatement node) throws BaseError;
    public T visit(ASTEmptystatement node) throws BaseError;
    public T visit(ASTExpressionstatement node) throws BaseError;
    public T visit(ASTForstatement node) throws BaseError;
    public T visit(ASTIfstatement node) throws BaseError;
    public T visit(ASTReturnstatement node) throws BaseError;
    public T visit(ASTVarstatement node) throws BaseError;
    public T visit(ASTWhilestatement node) throws BaseError;
}