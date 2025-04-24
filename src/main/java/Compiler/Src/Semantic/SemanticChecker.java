package Compiler.Src.Semantic;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.AST.Node.ASTNode;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.AST.Node.ExprNode.*;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;
import Compiler.Src.AST.Node.StatementNode.*;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.ScopeUtil.*;

@lombok.Getter
@lombok.Setter

public class SemanticChecker extends ScopeControl implements ASTVisitor<SMCError> {
    public SMCError visit(ASTNode node) throws BaseError {
        return new SMCError("SMC should not visit ASTNode\n");
    }

    public SMCError visit(ASTRoot node) throws BaseError {
        node.addScope(null);
        enterScope((GlobalScope) node.getScope());
        SMCError msg = new SMCError();
        for (ASTDef def : node.getDefNodes()) {
            msg.append(def.accept(this));
        }
        exitScope();
        if (msg.hasError()) {
            throw msg;
        }
        return msg;
    }

    public SMCError visit(ASTClassDef node) throws BaseError {
        node.addScope(currentScope);
        enterScope((ClassScope) node.getScope());
        SMCError msg = new SMCError();
        msg.append(node.getConstructor().accept(this));
        for (ASTFuncDef def : node.getFuncs()) {
            msg.append(def.accept(this));
        }
        exitScope();
        return msg;
    }

    public SMCError visit(ASTFuncDef node) throws BaseError// 处理传参
    {
        node.addScope(currentScope);
        enterScope((FuncScope) node.getScope());
        SMCError msg = new SMCError();
        for (ASTVarDef param : node.getParams()) {
            msg.append(param.accept(this));
        }
        msg.append(node.getBlockedBody().accept(this));
        if (!((FuncInfo) node.getInfo()).getFunctype().equals(GlobalScope.voidType)
                && !node.getInfo().getName().equals("main")) {
            if (!((FuncScope) currentScope).isExited()) {
                throw new SMCError("Missing Return Statement\n");
            }
        }
        exitScope();
        return msg;
    }

    public SMCError visit(ASTVarDef node) throws BaseError {
        SMCError msg = new SMCError();
        VarInfo info = (VarInfo) node.getInfo();
        if (!ValidVarType(info.getType())) {
            throw new SMCError("Invalid Type\n");
        } else if ((currentScope instanceof GlobalScope && currentScope.containsFuncs(node.findName()) != null)
                || (currentScope.containsVars(node.findName()) != null)) {
            throw new SMCError("Mutilple Definitions\n");
        } else {
            if (node.getInitexpr() != null) {
                msg.append(node.getInitexpr().accept(this));
                BaseInfo type = node.getInitexpr().getInfo().getDepTypeInfo();
                if (!LRTypeCheck(info.getType(), type) && !(node.getInitexpr() instanceof ASTAtomExpr
                        && ((ASTAtomExpr) node.getInitexpr()).getConstarray() != null)) {
                    throw new SMCError("Type Mismatch\n");
                }
                if (node.getInitexpr() instanceof ASTAtomExpr
                        && ((ASTAtomExpr) node.getInitexpr()).getConstarray() != null) {
                    ASTAtomExpr rightAtom = (ASTAtomExpr) node.getInitexpr();
                    if (info.getType().getDepth() < ((TypeInfo) rightAtom.getInfo().getType()).getDepth()) {
                        throw new SMCError("Dimension Out Of Bound\n");
                    }
                    if (!rightAtom.getInfo().getType().getName().equals("void")
                            && !info.getType().getName().equals(rightAtom.getInfo().getType().getName())) {
                        throw new SMCError("Type Mismatch\n");
                    }
                }
            }
            currentScope.declare(new VarInfo(node.findName(), info.getType()));
        }
        return msg;
    }

    public SMCError visit(ASTArrayExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getArrayName().accept(this));
        BaseInfo type = node.getArrayName().getInfo().getDepTypeInfo();
        if (!(type instanceof TypeInfo) || ((TypeInfo) type).getDepth() == 0) {
            throw new SMCError("Dimension Out Of Bound\n");
        }
        msg.append(node.getIndex().accept(this));
        BaseInfo index_type = node.getIndex().getInfo().getDepTypeInfo();
        if (!(index_type instanceof TypeInfo) || !index_type.equals(GlobalScope.intType)) {
            throw new SMCError("Type Mismatch\n");
        }
        node.setInfo(new ExprInfo("ArrayExpr", new TypeInfo(type.getName(), ((TypeInfo) type).getDepth() - 1), true));
        return msg;
    }

    public SMCError visit(ASTAssignExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getLeft().accept(this));
        BaseInfo LexprInfo = node.getLeft().getInfo();
        msg.append(node.getRight().accept(this));
        BaseInfo RexprInfo = node.getRight().getInfo();
        if (!(LexprInfo instanceof ExprInfo && RexprInfo instanceof ExprInfo)) {
            throw new SMCError("Have none ExprInfo expression\n");
        }
        if (!LRTypeCheck(((ExprInfo) LexprInfo).getDepTypeInfo(), ((ExprInfo) RexprInfo).getDepTypeInfo())
                && !(node.getRight() instanceof ASTAtomExpr
                        && ((ASTAtomExpr) node.getRight()).getConstarray() != null)) {
            // if (!((ExprInfo) LexprInfo).getDepTypeInfo().equals(((ExprInfo)
            // RexprInfo).getDepTypeInfo())
            // && !((((ExprInfo) LexprInfo).getDepTypeInfo().getDepth() > 0
            // || (!((ExprInfo) LexprInfo).getDepTypeInfo().equals(GlobalScope.intType)
            // && !((ExprInfo) LexprInfo).getDepTypeInfo().equals(GlobalScope.boolType)
            // && !((ExprInfo) LexprInfo).getDepTypeInfo().equals(GlobalScope.stringType)))
            // && ((ExprInfo) RexprInfo).getDepTypeInfo().equals(GlobalScope.nullType))
            // && !(node.getRight() instanceof ASTAtomExpr
            // && ((ASTAtomExpr) node.getRight()).getConstarray() != null)) {
            throw new SMCError("Type Mismatch\n");
        }
        if (node.getRight() instanceof ASTAtomExpr && ((ASTAtomExpr) node.getRight()).getConstarray() != null) {
            ASTAtomExpr rightAtom = (ASTAtomExpr) node.getRight();
            if (((ExprInfo) LexprInfo).getDepTypeInfo().getDepth() < ((TypeInfo) rightAtom.getInfo().getType())
                    .getDepth()) {
                throw new SMCError("Dimension Out Of Bound\n");
            }
            if (!rightAtom.getInfo().getType().getName().equals("void") && !((ExprInfo) LexprInfo).getDepTypeInfo()
                    .getName().equals(rightAtom.getInfo().getType().getName())) {
                throw new SMCError("Invalid Type\n");
            }
        }
        if (!((ExprInfo) LexprInfo).isLvalue()) {
            throw new SMCError("left hand side is a left value\n");
        }
        node.setInfo(new ExprInfo("assignExpr", ((ExprInfo) LexprInfo).getDepTypeInfo(), true));
        return msg;
    }

    public SMCError visit(ASTAtomExpr node) throws BaseError {
        SMCError msg = new SMCError();
        // System.err.println(node.getPos().str());
        if (node.getAtomType() == ASTAtomExpr.Type.INT) {
            node.setInfo(new ExprInfo("atomExpr", GlobalScope.intType, false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.BOOL) {
            node.setInfo(new ExprInfo("atomExpr", GlobalScope.boolType, false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.STRING) {
            node.setInfo(new ExprInfo("atomExpr", GlobalScope.stringType, false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.NULL) {
            node.setInfo(new ExprInfo("atomExpr", GlobalScope.nullType, false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.FSTRING) {
            msg.append(node.getFstring().accept(this));
            node.setInfo(new ExprInfo("atomExpr", GlobalScope.stringType, false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.CONSTARRAY) {
            msg.append(node.getConstarray().accept(this));
            node.setInfo(new ExprInfo("atomExpr", node.getConstarray().getInfo().getType(), false));
        } else if (node.getAtomType() == ASTAtomExpr.Type.INDENTIFIER) {
            // System.err.println(node.getPos().str());
            BaseInfo info = currentScope.BackSearch(node.getValue());
            if (info == null) {
                throw new SMCError("Undefined Identifier\n");
            } else if (info instanceof VarInfo) {
                node.setInfo(new ExprInfo("atomExpr", info, true));
            } else if (info instanceof FuncInfo) {
                node.setInfo(new ExprInfo("atomExpr", info, false));
            } else {
                throw new SMCError("Identifier is possiblily a class name, shouldn't be here\n");
            }
        } else if (node.getAtomType() == ASTAtomExpr.Type.THIS) {
            BaseScope scope = whichClass(currentScope);
            if (scope == null) {
                throw new SMCError("Invalid use of THIS out of a ClassScope\n");
            }
            node.setInfo(new ExprInfo("atomExpr", new TypeInfo(scope.getInfo().getName(), 0), false));
        } else {
            throw new SMCError("Invalid AtomExprn\n");
        }
        // System.err.println(node.getPos().str());
        // if(node.getInfo()==null){
        // System.err.println(node.getPos().str());
        // }
        return msg;
    }

    public SMCError visit(ASTBinaryExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getLeft().accept(this));
        msg.append(node.getRight().accept(this));
        TypeInfo Ltype = null;
        if (node.getLeft().getInfo().getType() instanceof TypeInfo) {
            Ltype = (TypeInfo) node.getLeft().getInfo().getType();
        } else if (node.getLeft().getInfo().getType() instanceof VarInfo) {
            Ltype = (TypeInfo) ((VarInfo) node.getLeft().getInfo().getType()).getType();
        } else {
            throw new SMCError("Invalid Ltype\n");
        }
        TypeInfo Rtype = null;
        if (node.getRight().getInfo().getType() instanceof TypeInfo) {
            Rtype = (TypeInfo) node.getRight().getInfo().getType();
        } else if (node.getRight().getInfo().getType() instanceof VarInfo) {
            Rtype = (TypeInfo) ((VarInfo) node.getRight().getInfo().getType()).getType();
        } else {
            throw new SMCError("Invalid Rtype\n");
        }
        if (!LRTypeCheck(Ltype, Rtype)) {
            throw new SMCError("Type Mismatch\n");
        }

        // if (!Ltype.equals(Rtype)
        // && !((Ltype.getDepth() > 0 || (!Ltype.equals(GlobalScope.intType) &&
        // !Ltype.equals(GlobalScope.boolType)
        // && !Ltype.equals(GlobalScope.stringType))) &&
        // Rtype.equals(GlobalScope.nullType))) {
        // throw new SMCError("Invalid BinarryExpr between different Type\n" +
        // node.getPos().str());
        // }

        if (Ltype.getDepth() > 0) {
            if (!(node.getOp().equals("==") || node.getOp().equals("!="))) {
                throw new SMCError("Invalid Type\n");
            }
        } else if (Ltype.equals(GlobalScope.intType)) {
            if (node.getOp().equals("!")) {
                throw new SMCError("Invalid Type\n");
            }
        } else if (Ltype.equals(GlobalScope.boolType)) {
            if (!(node.getOp().equals("==") || node.getOp().equals("!=") || node.getOp().equals("&&")
                    || node.getOp().equals("||"))) {
                throw new SMCError("Invalid Type\n");
            }
        } else if (Ltype.equals(GlobalScope.stringType) || Rtype.equals(GlobalScope.stringType)) {
            if (!(node.getOp().equals("==") || node.getOp().equals("!=") || node.getOp().equals("+")
                    || node.getOp().equals("<") || node.getOp().equals(">") || node.getOp().equals("<=")
                    || node.getOp().equals(">="))) {
                throw new SMCError("Invalid Type\n");
            }
        } else if (Ltype.equals(GlobalScope.nullType)) {
            if (!(node.getOp().equals("==") || node.getOp().equals("!="))) {
                throw new SMCError("Invalid Type\n");
            }
        } else {
            if (!Rtype.equals(GlobalScope.nullType)) {
                throw new SMCError("Invalid Type\n");
            }
        }
        if (node.getOp().equals("<") || node.getOp().equals(">") || node.getOp().equals("<=")
                || node.getOp().equals(">=")
                || node.getOp().equals("==") || node.getOp().equals("!=") || node.getOp().equals("&&")
                || node.getOp().equals("||")) {
            node.setInfo(new ExprInfo("binaryExpr", GlobalScope.boolType, false));
        } else {
            node.setInfo(new ExprInfo("binaryExpr", Ltype, false));
        }
        return msg;
    }

    public SMCError visit(ASTCallExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getFunc().accept(this));
        for (ASTExpr def : node.getArgs()) {
            msg.append(def.accept(this));
        }
        BaseInfo functype = node.getFunc().getInfo().getType();
        if (!(functype instanceof FuncInfo)) {
            throw new SMCError("Undefined Identifier\n");
        }
        if (node.getArgs().size() != ((FuncInfo) functype).getParams().size()) {
            throw new SMCError("Undefined Identifier\n");
        }
        for (int i = 0; i < ((FuncInfo) functype).getParams().size(); ++i) {
            TypeInfo type = ((FuncInfo) functype).getParams().get(i);
            ExprInfo exprInfo = node.getArgs().get(i).getInfo();
            TypeInfo recv = exprInfo.getDepTypeInfo();
            if (recv == null) {
                throw new SMCError("Undefined Identifier\n");
            }
            if (!LRTypeCheck(type, recv) && !type.equals(GlobalScope.thisType)) {
                throw new SMCError("Type Mismatch\n");
            }
            // if (!recv.equals(type) && !(recv.equals(GlobalScope.nullType) &&
            // (type.getDepth() > 0
            // || (!type.equals(GlobalScope.intType) && !type.equals(GlobalScope.boolType)
            // && !type.equals(GlobalScope.stringType))))
            // && !type.equals(GlobalScope.thisType)) {
            // throw new SMCError("args not match with input\n" + node.getPos().str());
            // }
        }
        node.setInfo(new ExprInfo("callExpr", ((FuncInfo) functype).getFunctype(), false));
        return msg;
    }

    public SMCError visit(ASTConditionalExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getQues().accept(this));
        msg.append(node.getLeft().accept(this));
        msg.append(node.getRight().accept(this));
        TypeInfo quesType = node.getQues().getInfo().getDepTypeInfo();
        TypeInfo leftType = node.getLeft().getInfo().getDepTypeInfo();
        TypeInfo rightType = node.getRight().getInfo().getDepTypeInfo();

        if (quesType == null || leftType == null || rightType == null) {
            throw new SMCError("Invalid Expr of conditionalExpr\n");
        }
        if (!quesType.equals(GlobalScope.boolType)) {
            throw new SMCError("Ques is not a boolean type\n");
        }
        if (!LRTypeCheck(leftType, rightType)) {
            throw new SMCError("Type Mismatch\n");
        }
        // if (!leftType.equals(rightType) || ) {
        // throw new SMCError("Lhs and Rhs type not match in conditionalExpr\n");
        // }

        node.setInfo(new ExprInfo("conditionalExpr", leftType, false));
        return msg;
    }

    public SMCError visit(ASTMemberExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getMember().accept(this));
        TypeInfo type = node.getMember().getInfo().getDepTypeInfo();
        if (type == null) {
            throw new SMCError("Invalid type in MemberExpr\n");
        }
        if (type.getDepth() > 0) {
            if (!node.getMemberName().equals("size")) {
                throw new SMCError("Unexpected member call for depth-array\n");
            } else {
                node.setInfo(new ExprInfo("memberExpr", GlobalScope.arraySize, false));
            }
        } else {
            ClassInfo classinfo = globalScope.containsClasses(node.getMember().getInfo().getDepTypeInfo().getName());
            if (classinfo == null) {
                throw new SMCError("no such class\n");
            }
            BaseInfo info = classinfo.get(node.getMemberName());
            if (info instanceof VarInfo) {
                node.setInfo(new ExprInfo("memberExpr", info, true));
            } else if (info instanceof FuncInfo) {
                node.setInfo(new ExprInfo("callExpr", info, false));
            } else {
                throw new SMCError("Undefined Identifier\n");
            }
        }
        return msg;
    }

    public SMCError visit(ASTNewExpr node) throws BaseError {
        SMCError msg = new SMCError();
        ClassInfo clsinfo = globalScope.containsClasses(node.getType().getName());
        if (clsinfo == null) {
            throw new SMCError("no such class in newExpr\n");
        }
        if ((clsinfo.getName().equals("int") || clsinfo.getName().equals("string") || clsinfo.getName().equals("bool")
                || clsinfo.getName().equals("void") || clsinfo.getName().equals("null"))
                && node.getType().getDepth() == 0) {
            throw new SMCError("default type not support newExpr\n");
        }
        for (ASTExpr unit : node.getSize()) {
            msg.append(unit.accept(this));
            if (unit.getInfo().getDepTypeInfo() == null
                    || !unit.getInfo().getDepTypeInfo().equals(GlobalScope.intType)) {
                throw new SMCError("size must be Integer in newExpr\n");
            }
        }
        if (node.getConstarray() != null) {
            msg.append(node.getConstarray().accept(this));
            if (node.getType().getDepth() < node.getConstarray().getInfo().getDepTypeInfo().getDepth()) {
                throw new SMCError("Type Mismatch\n");
            }
            if (!node.getConstarray().getInfo().getDepTypeInfo().getName().equals("void")
                    && !node.getType().equals(node.getConstarray().getInfo().getDepTypeInfo())) {
                throw new SMCError("constarray not match\n");
            }
        }
        if (node.getSize().size() == 0 && node.getConstarray() == null && node.getType().getDepth() != 0) {
            throw new SMCError("Invalid newExpr\n");
        }
        node.setInfo(new ExprInfo("newExpr", node.getType(), true));
        return msg;
    }

    public SMCError visit(ASTParenExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getExpr().accept(this));
        node.setInfo(
                new ExprInfo("parenExpr", node.getExpr().getInfo().getType(), node.getExpr().getInfo().isLvalue()));
        return msg;
    }

    public SMCError visit(ASTPreunaryExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getExpr().accept(this));
        TypeInfo type = node.getExpr().getInfo().getDepTypeInfo();
        if (type == null) {
            throw new SMCError("Invalid type in preunaryExpr\n");
        }
        if (node.getOp().equals("!")) {
            if (!type.equals(GlobalScope.boolType)) {
                throw new SMCError("Not boolean after ! in preunaryExpr\n");
            }
            node.setInfo(new ExprInfo("preunaryExpr", type, false));
        } else {
            if (!type.equals(GlobalScope.intType)) {
                throw new SMCError("only support Integer here in preunaryExpr\n");
            }
            if (node.getOp().equals("++") || node.getOp().equals("--")) {
                if (!node.getExpr().getInfo().isLvalue()) {
                    throw new SMCError("preunaryExpr not support due to not Lvalue\n");
                }
            }
            node.setInfo(new ExprInfo("preunaryExpr", type, true));
        }
        return msg;
    }

    public SMCError visit(ASTUnaryExpr node) throws BaseError {
        SMCError msg = new SMCError();
        msg.append(node.getExpr().accept(this));
        TypeInfo type = node.getExpr().getInfo().getDepTypeInfo();
        if (type == null) {
            throw new SMCError("Invalid type in unaryExpr\n");
        }
        if (!type.equals(GlobalScope.intType)) {
            throw new SMCError("only support Integer here in unaryExpr\n");
        }
        if (!node.getExpr().getInfo().isLvalue()) {
            throw new SMCError("unaryExpr not support due to not Lvalue\n");
        }
        node.setInfo(new ExprInfo("preunaryExpr", type, false));
        return msg;
    }

    public SMCError visit(ASTConstarray node) throws BaseError {
        SMCError msg = new SMCError();
        int depth = 0;
        int maxmaze = 0;
        TypeInfo typecheck = null;
        for (ASTExpr unit : node.getExpr()) {
            int tmpmaze = 0, tmpdep = 0;
            TypeInfo tmptype = GlobalScope.voidType;
            msg.append(unit.accept(this));

            if (!(unit.getInfo().getType() instanceof TypeInfo)) {
                throw new SMCError("constarray only contains TypeInfo\n");
            }
            tmptype = (TypeInfo) ((ASTAtomExpr) unit).getInfo().getType();
            if (unit.getInfo().getName().equals("atomExpr")) {
                if (unit instanceof ASTAtomExpr) {
                    if (((ASTAtomExpr) unit).getConstarray() == null) {
                        tmpmaze = 1;
                        tmpdep = 1;
                    } else {
                        tmpmaze = ((ASTAtomExpr) unit).getConstarray().getMaze() + 1;
                        tmpdep = ((ASTAtomExpr) unit).getConstarray().getDep() == 0 ? 0
                                : ((ASTAtomExpr) unit).getConstarray().getDep() + 1;
                    }
                } else {
                    throw new SMCError("constarray Atom error\n");
                }
            } else {
                tmpmaze = 1;
                tmpdep = 1;
                tmptype = (TypeInfo) unit.getInfo().getType();
            }
            if (!tmptype.getName().equals(GlobalScope.voidType.getName()) && typecheck != null
                    && !tmptype.equals(typecheck)) {
                throw new SMCError("different type in const array\n");
            } else if (tmpdep > 0 && depth > 0 && tmpdep != depth) {
                throw new SMCError("each expr in costarray must have the same depth\n" + unit.getPos().str());
            } else if (((ExprInfo) unit.getInfo()).isLvalue()) {
                throw new SMCError("constarray shouldn't contain Lvalue expression\n");
            }
            if (tmpdep > 0) {
                depth = tmpdep;
            }
            if (!tmptype.getName().equals(GlobalScope.voidType.getName())) {
                typecheck = tmptype;
            }
            if (tmpmaze > maxmaze) {
                maxmaze = tmpmaze;
            }
        }
        // depth = depth + 1;
        if (maxmaze == 0) {
            maxmaze = 1;
        }
        node.setDep(depth);
        node.setMaze(maxmaze);
        node.setInfo(new ExprInfo("constarrayExpr",
                new TypeInfo(typecheck != null ? typecheck.getName() : GlobalScope.voidType.getName(), maxmaze),
                false));
        return msg;
    }

    public SMCError visit(ASTFstring node) throws BaseError {
        SMCError msg = new SMCError();
        if (node.getExprpart() != null) {
            for (ASTExpr unit : node.getExprpart()) {
                msg.append(unit.accept(this));
                TypeInfo type = unit.getInfo().getDepTypeInfo();
                if (type == null) {
                    throw new SMCError("Type Mismatch\n");
                }
                if (!(type.equals(GlobalScope.intType) || type.equals(GlobalScope.boolType)
                        || type.equals(GlobalScope.stringType))) {
                    throw new SMCError("Type Mismatch\n");
                }
            }
        }
        node.setInfo(new ExprInfo("fstringExpr", GlobalScope.stringType, false));
        return msg;
    }

    public SMCError visit(ASTBlockstatement node) throws BaseError {
        node.addScope(currentScope);
        enterScope(node.getScope());
        SMCError msg = new SMCError();
        for (ASTStatement stmt : node.getStmts()) {
            msg.append(stmt.accept(this));
        }
        exitScope();
        return msg;
    }

    public SMCError visit(ASTBreakstatement node) {
        if (whichLoop(currentScope) == null) {
            throw new SMCError("Invalid Control Flow\n");
        }
        return new SMCError();
    }

    public SMCError visit(ASTContinuestatement node) {
        if (whichLoop(currentScope) == null) {
            throw new SMCError("Invalid Control Flow\n");
        }
        return new SMCError();
    }

    public SMCError visit(ASTEmptystatement node) {
        return new SMCError();
    }

    public SMCError visit(ASTExpressionstatement node) throws BaseError {
        SMCError msg = new SMCError();
        for (ASTExpr unit : node.getExpr()) {
            msg.append(unit.accept(this));
            // if (unit.getInfo().getName().equals("atomExpr")) {
            // throw new SMCError("incomplete expression which is atom\n");
            // }
        }
        return msg;
    }

    public SMCError visit(ASTForstatement node) throws BaseError {
        node.addScope(currentScope);
        enterScope(node.getScope());
        SMCError msg = new SMCError();
        if (node.getVarinit() != null) {
            msg.append(node.getVarinit().accept(this));
        } else if (node.getExprinit() != null) {
            msg.append(node.getExprinit().accept(this));
        }

        if (node.getCond() != null) {
            msg.append(node.getCond().accept(this));
            TypeInfo type = ((ExprInfo) node.getCond().getInfo()).getDepTypeInfo();
            if (type == null || !type.equals(GlobalScope.boolType)) {
                throw new SMCError("Invalid Type\n");
            }
        }
        if (node.getStep() != null) {
            msg.append(node.getStep().accept(this));
        }

        msg.append(node.getStmts().accept(this));
        exitScope();
        return msg;
    }

    public SMCError visit(ASTIfstatement node) throws BaseError {
        node.addIfScope(currentScope);
        enterScope(node.getIfScope());
        SMCError msg = new SMCError();
        msg.append(node.getJudge().accept(this));
        TypeInfo type = ((ExprInfo) node.getJudge().getInfo()).getDepTypeInfo();
        if (type == null || !type.equals(GlobalScope.boolType)) {
            throw new SMCError("Invalid Type\n");
        }
        msg.append(node.getIfstmt().accept(this));
        exitScope();
        if (node.getElsestmt() != null) {
            node.addElseScope(currentScope);
            enterScope(node.getElseScope());
            msg.append(node.getElsestmt().accept(this));
            exitScope();
        }
        return msg;
    }

    public SMCError visit(ASTReturnstatement node) throws BaseError {
        SMCError msg = new SMCError();
        FuncScope funcScope = (FuncScope) whichFunc(currentScope);
        if (funcScope == null) {
            throw new SMCError("throw should be used in Function\n");
        }
        TypeInfo funcType = ((FuncInfo) funcScope.getInfo()).getFunctype();
        if (node.getRet() == null) {
            if (!funcType.equals(GlobalScope.voidType)) {
                throw new SMCError("No return type not match\n");
            }
        } else {
            msg.append(node.getRet().accept(this));
            TypeInfo retType = ((ExprInfo) node.getRet().getInfo()).getDepTypeInfo();
            if ((retType == null || !retType.equals(funcType)) && !((retType != null
                    && retType.equals(GlobalScope.nullType))
                    && (funcType.getDepth() > 0 || (!funcType.equals(GlobalScope.intType)
                            && !funcType.equals(GlobalScope.boolType) && !funcType.equals(GlobalScope.stringType))))) {
                throw new SMCError("Type Mismatch\n");
            }
        }
        funcScope.setExit(true);
        return msg;
    }

    public SMCError visit(ASTVarstatement node) throws BaseError {
        SMCError msg = new SMCError();
        for (ASTVarDef def : node.getVarDefs()) {
            msg.append(def.accept(this));
        }
        return msg;
    }

    public SMCError visit(ASTWhilestatement node) throws BaseError {
        node.addScope(currentScope);
        enterScope(node.getScope());
        SMCError msg = new SMCError();
        msg.append(node.getJudge().accept(this));
        TypeInfo type = ((ExprInfo) node.getJudge().getInfo()).getDepTypeInfo();
        if (type == null || !type.equals(GlobalScope.boolType)) {
            throw new SMCError("Invalid Type\n");
        }
        msg.append(node.getStmts().accept(this));
        exitScope();
        return msg;
    }
}
