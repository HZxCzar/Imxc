package Compiler.Src.AST;

import java.util.ArrayList;

import Compiler.Src.AST.Node.ASTNode;
import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.AST.Node.ExprNode.*;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;
import Compiler.Src.AST.Node.StatementNode.*;
import Compiler.Src.Grammer.*;
import Compiler.Src.Util.position;
import Compiler.Src.Util.Error.ASTError;
import Compiler.Src.Util.Info.ClassInfo;
import Compiler.Src.Util.Info.FuncInfo;
import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.Util.Info.VarInfo;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(MxParser.ProgramContext ctx) {
        var defines = new ArrayList<ASTDef>();
        for (var def : ctx.children) {
            if ((def instanceof MxParser.FunDefContext) || (def instanceof MxParser.ClassDefContext)) {
                defines.add((ASTDef) visit(def));
            } else if (def instanceof MxParser.VarDefContext) {
                var varDefs = (ASTVarstatement) visit(def);
                for (ASTVarDef varDef : varDefs.getVarDefs()) {
                    defines.add(varDef);
                }
            }
        }
        return ASTRoot.builder().parent(null).pos(new position(ctx.start)).DefNodes(defines).build();
    }

    @Override
    public ASTNode visitFunDef(MxParser.FunDefContext ctx) {
        var paramlist = ctx.funParaList();
        var params = new ArrayList<ASTVarDef>();
        if (paramlist != null) {
            for (var param : paramlist.funVarDef()) {
                var init = param.atomVarDef();
                if (param.typeVarDef().arrayUnit().size() != 0) {
                    for (var unit : param.typeVarDef().arrayUnit()) {
                        if (unit.expression() != null) {
                            throw new ASTError("Invalid define of array " + ctx.start);
                        }
                    }
                }
                params.add(ASTVarDef.builder().pos(new position(param.start))
                        .info(new VarInfo(init.Identifier().getText(),
                                new TypeInfo(param.typeVarDef().type().getText(),
                                        param.typeVarDef().arrayUnit().size())))
                        .initexpr(init.expression() != null ? (ASTExpr) visit(init.expression()) : null).build());
            }
        }
        if (ctx.typeVarDef().arrayUnit().size() != 0) {
            for (var unit : ctx.typeVarDef().arrayUnit()) {
                if (unit.expression() != null) {
                    throw new ASTError("Invalid define of array " + ctx.start);
                }
            }
        }
        ASTFuncDef FunDef = ASTFuncDef.builder().pos(new position(ctx.start))
                .info(new FuncInfo(ctx.Identifier().getText(),
                        new TypeInfo(ctx.typeVarDef().type().getText(), ctx.typeVarDef().arrayUnit().size()), params))
                .params(params)
                .blockedBody((ASTBlockstatement) visit(ctx.blockstatement())).build();
        for (var p : params) {
            p.setParent(FunDef);
        }
        for (var stmt : FunDef.getBlockedBody().getStmts()) {
            stmt.setParent(FunDef);
        }
        return FunDef;
    }

    @Override
    public ASTNode visitClassDef(MxParser.ClassDefContext ctx) {
        ASTFuncDef constructor = null;
        if (ctx.classBuild().size() == 0) {
            // default
            constructor = ASTFuncDef.builder().pos(new position(ctx.start))
                    .info(new FuncInfo(ctx.Identifier().getText(), new TypeInfo("void", 0), new ArrayList<>()))
                    .params(new ArrayList<>())
                    .blockedBody(ASTBlockstatement.builder().pos(new position(ctx.start)).stmts(new ArrayList<>())
                            .build())
                    .build();
        } else if (ctx.classBuild().size() == 1) {
            var constru = ctx.classBuild(0);
            if (!constru.Identifier().getText().equals(ctx.Identifier().getText())) {
                throw new ASTError("Class constructor has a different name to class " + ctx.Identifier() + ' '
                        + (new position(ctx.classBuild(0).start).str()));
            }
            constructor = ASTFuncDef.builder().pos(new position(constru.start))
                    .info(new FuncInfo(ctx.Identifier().getText(), new TypeInfo("void", 0), new ArrayList<>()))
                    .params(new ArrayList<>())
                    .blockedBody((ASTBlockstatement) visit(constru.blockstatement()))
                    .build();
        } else {
            throw new ASTError("More than one constructor for Class " + ctx.Identifier() + ctx.start);
        }
        var vars = new ArrayList<ASTVarDef>();
        for (var v : ctx.varDef()) {
            var varDef = (ASTVarstatement) visit(v);
            for (var unit : varDef.getVarDefs()) {
                vars.add(unit);
            }
        }
        var funcs = new ArrayList<ASTFuncDef>();
        for (var Fs : ctx.funDef()) {
            if (Fs.Identifier().getText().equals(ctx.Identifier().getText())) {
                throw new ASTError(
                        "Function name can not be the same with its Class's name " + ctx.Identifier() + ctx.start);
            }
            funcs.add((ASTFuncDef) visit(Fs));
        }
        ASTClassDef ClassDef = ASTClassDef.builder().pos(new position(ctx.start))
                .info(new ClassInfo(ctx.Identifier().getText(), constructor, vars, funcs)).constructor(constructor)
                .vars(vars)
                .funcs(funcs).build();
        for (var v : vars) {
            v.setParent(ClassDef);
        }
        for (var f : funcs) {
            f.setParent(ClassDef);
        }
        constructor.setParent(ClassDef);
        return ClassDef;
    }

    @Override
    public ASTNode visitVarDef(MxParser.VarDefContext ctx) {
        for (var ary : ctx.typeVarDef().arrayUnit()) {
            if (ary.expression() != null) {
                throw new ASTError("Invalid define of array " + ctx.start);
            }
        }
        TypeInfo type = new TypeInfo(ctx.typeVarDef().type().getText(), ctx.typeVarDef().arrayUnit().size());
        var deflist = new ArrayList<ASTVarDef>();
        for (var defunit : ctx.atomVarDef()) {
            deflist.add(ASTVarDef.builder().pos(new position(defunit.start))
                    .info(new VarInfo(defunit.Identifier().getText(), type))
                    .initexpr(defunit.expression() != null ? (ASTExpr) visit(defunit.expression()) : null).build());
        }
        ASTVarstatement Varstatement = ASTVarstatement.builder().pos(new position(ctx.start)).VarDefs(deflist).build();
        for (var v : deflist) {
            v.setParent(Varstatement);
        }
        return Varstatement;
    }

    // @Override
    // public ASTNode visitStatment(MxParser.StatementContext ctx) {
    // return visitChildren(ctx);
    // }

    @Override
    public ASTNode visitBlockstatement(MxParser.BlockstatementContext ctx) {
        var stmts = new ArrayList<ASTStatement>();
        for (var stmt : ctx.statement()) {
            stmts.add((ASTStatement) visit(stmt));
        }
        ASTBlockstatement block = ASTBlockstatement.builder().pos(new position(ctx.start)).stmts(stmts).build();
        for (var stmt : stmts) {
            stmt.setParent(block);
        }
        return block;
    }

    @Override
    public ASTNode visitIfstatement(MxParser.IfstatementContext ctx) {
        // var judge = ctx.expression();
        ASTIfstatement Ifstmt = ASTIfstatement.builder().pos(new position(ctx.start))
                .judge((ASTExpr) visit(ctx.expression()))
                .ifstmt((ASTStatement) visit(ctx.statement(0)))
                .elsestmt(ctx.statement().size() > 1 ? (ASTStatement) visit(ctx.statement(1)) : null).build();
        Ifstmt.getJudge().setParent(Ifstmt);
        Ifstmt.getIfstmt().setParent(Ifstmt);
        if (Ifstmt.getElsestmt() != null) {
            Ifstmt.getElsestmt().setParent(Ifstmt);
        }
        return Ifstmt;
    }

    @Override
    public ASTNode visitWhilestatement(MxParser.WhilestatementContext ctx) {
        // var judge = ctx.expression();
        ASTWhilestatement whileStmt = ASTWhilestatement.builder().pos(new position(ctx.start))
                .judge((ASTExpr) visit(ctx.expression()))
                .stmts((ASTStatement) visit(ctx.statement()))
                .build();
        whileStmt.getJudge().setParent(whileStmt);
        whileStmt.getStmts().setParent(whileStmt);
        return whileStmt;
    }

    @Override
    public ASTNode visitForstatement(MxParser.ForstatementContext ctx) {
        ASTForstatement forstmt = null;
        // if (ctx.forinit() != null) {
        if (ctx.forinit().expression() != null) {
            forstmt = ASTForstatement.builder().pos(new position(ctx.start)).Varinit(null)
                    .Exprinit((ASTExpr) visit(ctx.forinit().expression()))
                    .cond(ctx.condition != null ? (ASTExpr) visit(ctx.condition) : null)
                    .step(ctx.update != null ? (ASTExpr) visit(ctx.update) : null)
                    .stmts((ASTStatement) visit(ctx.statement())).build();
        } else if (ctx.forinit().varDef() != null) {
            forstmt = ASTForstatement.builder().pos(new position(ctx.start))
                    .Varinit((ASTVarstatement) visit(ctx.forinit().varDef()))
                    .Exprinit(null)
                    .cond(ctx.condition != null ? (ASTExpr) visit(ctx.condition) : null)
                    .step(ctx.update != null ? (ASTExpr) visit(ctx.update) : null)
                    .stmts((ASTStatement) visit(ctx.statement())).build();
        } else {
            forstmt = ASTForstatement.builder().pos(new position(ctx.start))
                    .Varinit(null)
                    .Exprinit(null)
                    .cond(ctx.condition != null ? (ASTExpr) visit(ctx.condition) : null)
                    .step(ctx.update != null ? (ASTExpr) visit(ctx.update) : null)
                    .stmts((ASTStatement) visit(ctx.statement())).build();
        }
        // } else {
        // forstmt = ASTForstatement.builder().pos(new position(ctx.start))
        // .Varinit(null)
        // .Exprinit(null)
        // .cond(ctx.condition != null ? (ASTExpr) visit(ctx.condition) : null)
        // .step(ctx.update != null ? (ASTExpr) visit(ctx.update) : null)
        // .stmts((ASTStatement) visit(ctx.statement())).build();
        // }
        if (forstmt.getVarinit() != null) {
            forstmt.getVarinit().setParent(forstmt);
        }
        if (forstmt.getExprinit() != null) {
            forstmt.getExprinit().setParent(forstmt);
        }
        if (forstmt.getCond() != null) {
            forstmt.getCond().setParent(forstmt);
        }
        if (forstmt.getStep() != null) {
            forstmt.getStep().setParent(forstmt);
        }
        forstmt.getStmts().setParent(forstmt);
        return forstmt;
    }

    @Override
    public ASTNode visitReturnstatement(MxParser.ReturnstatementContext ctx) {
        ASTReturnstatement returnStmt = ASTReturnstatement.builder().pos(new position(ctx.start))
                .ret(ctx.expression() != null ? (ASTExpr) visit(ctx.expression()) : null).build();
        if (returnStmt.getRet() != null) {
            returnStmt.getRet().setParent(returnStmt);
        }
        return returnStmt;
    }

    @Override
    public ASTNode visitBreakstatement(MxParser.BreakstatementContext ctx) {
        ASTBreakstatement breakStmt = ASTBreakstatement.builder().pos(new position(ctx.start)).build();
        return breakStmt;
    }

    @Override
    public ASTNode visitContinuestatement(MxParser.ContinuestatementContext ctx) {
        ASTContinuestatement continueStmt = ASTContinuestatement.builder().pos(new position(ctx.start)).build();
        return continueStmt;
    }

    @Override
    public ASTNode visitExpressionstatement(MxParser.ExpressionstatementContext ctx) {
        var exprs = new ArrayList<ASTExpr>();
        for (var unit : ctx.expression()) {
            exprs.add((ASTExpr) visit(unit));
        }
        ASTExpressionstatement exprStmt = ASTExpressionstatement.builder().pos(new position(ctx.start)).expr(exprs)
                .build();
        for (var expr : exprs) {
            expr.setParent(exprStmt);
        }
        return exprStmt;
    }

    @Override
    public ASTNode visitEmptystatement(MxParser.EmptystatementContext ctx) {
        ASTEmptystatement emptyStmt = ASTEmptystatement.builder().pos(new position(ctx.start)).build();
        return emptyStmt;
    }

    @Override
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        boolean seqend = false;
        var units = new ArrayList<ASTExpr>();
        for (var unit : ctx.arrayUnit()) {
            if (unit.expression() == null) {
                seqend = true;
            } else {
                if (seqend) {
                    throw new ASTError("Array shape must be specified from left to right" + ctx.start);
                }
                units.add((ASTExpr) visit(unit.expression()));
            }
        }
        ASTNewExpr newExpr = ASTNewExpr.builder().pos(new position(ctx.start))
                .type(new TypeInfo(ctx.type().getText(), ctx.arrayUnit().size())).size(units)
                .constarray(ctx.constarray() != null ? (ASTConstarray) visit(ctx.constarray()) : null).build();
        for (var unit : units) {
            unit.setParent(newExpr);
        }
        if (ctx.constarray() != null) {
            newExpr.getConstarray().setParent(newExpr);
        }
        return newExpr;
    }

    @Override
    public ASTNode visitParenExpr(MxParser.ParenExprContext ctx) {
        ASTParenExpr parenExpr = ASTParenExpr.builder().pos(new position(ctx.start))
                .expr((ASTExpr) visit(ctx.expression())).build();
        parenExpr.getExpr().setParent(parenExpr);
        return parenExpr;
    }

    @Override
    public ASTNode visitCallExpr(MxParser.CallExprContext ctx) {
        var args = new ArrayList<ASTExpr>();
        if (ctx.callArgs() != null) {
            for (var arg : ctx.callArgs().expression()) {
                args.add((ASTExpr) visit(arg));
            }
        }
        ASTCallExpr callExpr = ASTCallExpr.builder().pos(new position(ctx.start))
                .func((ASTExpr) visit(ctx.expression()))
                .args(args).build();
        callExpr.getFunc().setParent(callExpr);
        for (var arg : args) {
            arg.setParent(callExpr);
        }
        return callExpr;
    }

    @Override
    public ASTNode visitMemberExpr(MxParser.MemberExprContext ctx) {
        ASTMemberExpr memberExpr = ASTMemberExpr.builder().pos(new position(ctx.start))
                .member((ASTExpr) visit(ctx.expression())).memberName(ctx.atom().getText()).build();
        memberExpr.getMember().setParent(memberExpr);
        return memberExpr;
    }

    @Override
    public ASTNode visitArrayExpr(MxParser.ArrayExprContext ctx) {
        ASTArrayExpr arrayExpr = ASTArrayExpr.builder().pos(new position(ctx.start))
                .arrayName((ASTExpr) visit(ctx.expression()))
                .index(ctx.arrayUnit().expression() != null ? (ASTExpr) visit(ctx.arrayUnit().expression())
                        : null)
                .build();
        arrayExpr.getArrayName().setParent(arrayExpr);
        if (arrayExpr.getIndex() != null) {
            arrayExpr.getIndex().setParent(arrayExpr);
        }
        return arrayExpr;
    }

    @Override
    public ASTNode visitPreunaryExpr(MxParser.PreunaryExprContext ctx) {
        ASTPreunaryExpr preUnaryExpr = ASTPreunaryExpr.builder().pos(new position(ctx.start))
                .expr((ASTExpr) visit(ctx.expression())).op(ctx.op.getText()).build();
        preUnaryExpr.getExpr().setParent(preUnaryExpr);
        return preUnaryExpr;
    }

    @Override
    public ASTNode visitUnaryExpr(MxParser.UnaryExprContext ctx) {
        ASTUnaryExpr unaryExpr = ASTUnaryExpr.builder().pos(new position(ctx.start))
                .expr((ASTExpr) visit(ctx.expression()))
                .op(ctx.op.getText()).build();
        unaryExpr.getExpr().setParent(unaryExpr);
        return unaryExpr;
    }

    @Override
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        ASTBinaryExpr binaryExpr = ASTBinaryExpr.builder().pos(new position(ctx.start))
                .left((ASTExpr) visit(ctx.expression(0)))
                .op(ctx.op.getText()).right((ASTExpr) visit(ctx.expression(1))).build();
        binaryExpr.getLeft().setParent(binaryExpr);
        binaryExpr.getRight().setParent(binaryExpr);
        return binaryExpr;
    }

    @Override
    public ASTNode visitConditionalExpr(MxParser.ConditionalExprContext ctx) {
        ASTConditionalExpr conditionalExpr = ASTConditionalExpr.builder().pos(new position(ctx.start))
                .Ques((ASTExpr) visit(ctx.expression(0))).left((ASTExpr) visit(ctx.expression(1)))
                .right((ASTExpr) visit(ctx.expression(2))).build();
        conditionalExpr.getQues().setParent(conditionalExpr);
        conditionalExpr.getLeft().setParent(conditionalExpr);
        conditionalExpr.getRight().setParent(conditionalExpr);
        return conditionalExpr;
    }

    @Override
    public ASTNode visitAssignExpr(MxParser.AssignExprContext ctx) {
        ASTAssignExpr assignExpr = ASTAssignExpr.builder().pos(new position(ctx.start))
                .left((ASTExpr) visit(ctx.expression(0)))
                .right((ASTExpr) visit(ctx.expression(1))).build();
        assignExpr.getLeft().setParent(assignExpr);
        assignExpr.getRight().setParent(assignExpr);
        return assignExpr;
    }

    @Override
    public ASTNode visitAtomExpr(MxParser.AtomExprContext ctx) {
        ASTAtomExpr atomExpr = null;
        ASTAtomExpr.Type atomType = null;
        String value = "";
        value = ctx.atom().getText();
        if (ctx.atom().Integer() != null) {
            atomType = ASTAtomExpr.Type.INT;
        } else if (ctx.atom().True() != null || ctx.atom().False() != null) {
            atomType = ASTAtomExpr.Type.BOOL;
        } else if (ctx.atom().This() != null) {
            atomType = ASTAtomExpr.Type.THIS;
        } else if (ctx.atom().Null() != null) {
            atomType = ASTAtomExpr.Type.NULL;
        } else if (ctx.atom().Identifier() != null) {
            // System.err.println("AtomExpr "+ctx.getText());
            atomType = ASTAtomExpr.Type.INDENTIFIER;
        } else if (ctx.atom().StringLiteral() != null) {
            atomType = ASTAtomExpr.Type.STRING;
            value = "";
            String str = ctx.atom().getText();
            str = str.substring(1, str.length() - 1);
            for (int i = 0; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (c == '\\') {
                    ++i;
                    char cc = str.charAt(i);
                    if (cc == 'n') {
                        value += '\n';
                    } else if (cc == '\"') {
                        value += '\"';
                    } else {
                        value += '\\';
                    }
                } else {
                    value += c;
                }
            }
        } else if (ctx.atom().fstring() != null) {
            atomType = ASTAtomExpr.Type.FSTRING;
            atomExpr = ASTAtomExpr.builder().pos(new position(ctx.start)).atomType(atomType).value(null)
                    .constarray(null).fstring((ASTFstring) visit(ctx.atom().fstring())).build();
            atomExpr.getFstring().setParent(atomExpr);
            return atomExpr;
        } else if (ctx.atom().constarray() != null) {
            atomType = ASTAtomExpr.Type.CONSTARRAY;
            atomExpr = ASTAtomExpr.builder().pos(new position(ctx.start)).atomType(atomType).value(null)
                    .constarray((ASTConstarray) visit(ctx.atom().constarray())).fstring(null).build();
            atomExpr.getConstarray().setParent(atomExpr);
            return atomExpr;
        }
        atomExpr = ASTAtomExpr.builder().pos(new position(ctx.start)).atomType(atomType).value(value).constarray(null)
                .fstring(null).build();
        return atomExpr;
    }

    @Override
    public ASTNode visitConstarray(MxParser.ConstarrayContext ctx) {
        var units = new ArrayList<ASTExpr>();
        for (var unit : ctx.expression()) {
            units.add((ASTExpr) visit(unit));
        }
        ASTConstarray constarrayExpr = ASTConstarray.builder().pos(new position(ctx.start)).expr(units).maze(0).dep(0)
                .build();
        for (var unit : units) {
            unit.setParent(constarrayExpr);
        }
        return constarrayExpr;
    }

    @Override
    public ASTNode visitFstring(MxParser.FstringContext ctx) {
        var strpart = new ArrayList<String>();
        var exprpart = new ArrayList<ASTExpr>();
        if (ctx.basestring != null) {
            strpart.add(ctx.FStringLiteral().getText().substring(2, ctx.FStringLiteral().getText().length() - 1));
            return ASTFstring.builder().pos(new position(ctx.start))
                    .strpart(strpart)
                    .exprpart(null).build();
        } else {
            strpart.add(ctx.FomatStringL().getText().substring(2, ctx.FomatStringL().getText().length() - 1));
            exprpart.add((ASTExpr) visit(ctx.expression()));
            for (var expr : ctx.midfstringUnit()) {
                strpart.add(expr.FomatStringM().getText().substring(1, expr.FomatStringM().getText().length() - 1));
                exprpart.add((ASTExpr) visit(expr.expression()));
            }
            strpart.add(ctx.FomatStringR().getText().substring(1, ctx.FomatStringR().getText().length() - 1));
            ASTFstring fstringExpr = ASTFstring.builder().pos(new position(ctx.start)).strpart(strpart)
                    .exprpart(exprpart)
                    .build();
            for (var expr : exprpart) {
                expr.setParent(fstringExpr);
            }
            return fstringExpr;
        }
    }
}