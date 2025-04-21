// Generated from Mx.g4 by ANTLR 4.13.1
package Compiler.Src.Grammer;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MxVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MxParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MxParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(MxParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#classDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDef(MxParser.ClassDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#typeVarDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeVarDef(MxParser.TypeVarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#varDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDef(MxParser.VarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#atomVarDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomVarDef(MxParser.AtomVarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#classBuild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBuild(MxParser.ClassBuildContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#blockstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockstatement(MxParser.BlockstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#funDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunDef(MxParser.FunDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#funVarDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunVarDef(MxParser.FunVarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#funParaList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunParaList(MxParser.FunParaListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MxParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#emptystatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptystatement(MxParser.EmptystatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#ifstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfstatement(MxParser.IfstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#whilestatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhilestatement(MxParser.WhilestatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#forinit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForinit(MxParser.ForinitContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#forstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForstatement(MxParser.ForstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#returnstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnstatement(MxParser.ReturnstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#breakstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakstatement(MxParser.BreakstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#continuestatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinuestatement(MxParser.ContinuestatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#expressionstatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionstatement(MxParser.ExpressionstatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#constarray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstarray(MxParser.ConstarrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#arrayUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayUnit(MxParser.ArrayUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#callArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallArgs(MxParser.CallArgsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(MxParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code preunaryExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreunaryExpr(MxParser.PreunaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(MxParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpr(MxParser.ArrayExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberExpr(MxParser.MemberExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(MxParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryExpr(MxParser.BinaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpr(MxParser.CallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignExpr(MxParser.AssignExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(MxParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code conditionalExpr}
	 * labeled alternative in {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpr(MxParser.ConditionalExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(MxParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#fstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFstring(MxParser.FstringContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#midfstringUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMidfstringUnit(MxParser.MidfstringUnitContext ctx);
}