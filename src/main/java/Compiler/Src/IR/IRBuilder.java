package Compiler.Src.IR;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.*;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.AST.Node.ExprNode.*;
import Compiler.Src.AST.Node.ExprNode.ExprUnitNode.*;
import Compiler.Src.AST.Node.StatementNode.*;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.IR.Node.*;
import Compiler.Src.IR.Node.Def.*;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.*;
import Compiler.Src.IR.Node.util.*;
import Compiler.Src.IR.Type.*;
import Compiler.Src.IR.Util.*;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.ClassInfo;
import Compiler.Src.Util.Info.ExprInfo;
import Compiler.Src.Util.Info.FuncInfo;
import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.Util.Info.VarInfo;
import Compiler.Src.Util.ScopeUtil.*;

public class IRBuilder extends IRControl implements ASTVisitor<IRNode> {
    int loopDepth;
    public IRBuilder(String fileName) {
        super(fileName);
        loopDepth = 0;
    }

    @Override
    public IRNode visit(ASTNode node) throws BaseError {
        throw new IRError("IRBuilder.visit(ASTNode) should not be called");
    }

    @Override
    public IRNode visit(ASTRoot node) throws BaseError {
        enterASTNode(node);
        loopDepth = 0;
        var program = new IRRoot();
        for (var def : node.getDefNodes()) {
            if (def instanceof ASTClassDef) {
                Boolean aline = true;
                var ClassNode = (ASTClassDef) def;
                var defs = new ArrayList<IRType>();
                IRType tmp = null;
                for (var vardef : ClassNode.getVars()) {
                    var irType = new IRType(((VarInfo) vardef.getInfo()).getType());
                    defs.add(irType);
                    if (tmp == null) {
                        tmp = irType;
                    } else if (!tmp.equals(irType)) {
                        aline = false;
                    }
                }
                var typename = "%class." + def.findName();
                initSize(typename, defs);
                var structtype = new IRStructType(typename, defs, aline);
                structtype.setSize(name2Size.get(typename));
                program.addDef(new IRGlobalDef(new IRVariable(structtype, typename)));
            }
        }
        IRLoop.initCount();
        for (var def : node.getDefNodes()) {
            if (def instanceof ASTVarDef) {
                program.addDef((IRGlobalDef) def.accept(this));
            }
        }
        for (var def : node.getDefNodes()) {
            if (def instanceof ASTClassDef) {
                IRLoop.initCount();
                var classDef = (IRClassDef) def.accept(this);
                for (var func : classDef.getFuncs()) {
                    program.addFunc(func);
                }
            }
        }
        for (var def : node.getDefNodes()) {
            if (def instanceof ASTFuncDef) {
                IRLoop.initCount();
                var funcDef = (IRFuncDef) def.accept(this);
                if (def.findName().equals("main")) {
                    funcDef.getBlockstmts().get(0)
                            .addFront(new IRCall(++InstCounter.InstCounter, "main.global.init", new ArrayList<>()));
                }
                program.addFunc(funcDef);
            }
        }
        var init = initFunc.getBlockstmts().get(initFunc.getBlockstmts().size() - 1);
        // init.addInsts(new IRRet());
        init.setReturnInst(new IRRet(++InstCounter.InstCounter));
        program.addFunc(initFunc, 0);
        for (var str : strDefs) {
            program.addDef(str);
        }
        exitASTNode(node);
        return program;
    }

    @Override
    public IRNode visit(ASTFuncDef node) throws BaseError {
        enterASTNode(node);
        loopDepth = 0;
        var functype = new IRType(node.getReturnType());
        var params = new ArrayList<IRVariable>();
        for (var param : node.getParams()) {
            if (!param.findName().equals("this")) {
                currentScope.IRdeclare(param.findName());
            }
            params.add(new IRVariable(new IRType(param.getVarType()),
                    getVarName(param.findName(), currentScope) + ".param"));
        }
        var stmts = new IRStmt();
        stmts.addBlockInsts((IRStmt) node.getBlockedBody().accept(this));
        // for (var stmt : node.getBlockedBody().getStmts()) {
        // stmts.addInsts((IRInst) stmt.accept(this));
        // }
        if (node.findName().equals("main")) {
            stmts.addInsts(new IRRet(++InstCounter.InstCounter, new IRLiteral(GlobalScope.irIntType, "0")));
        }
        var func = new IRFuncDef(node.findName(), params, functype, stmt2block(stmts, functype));
        exitASTNode(node);
        return func;
    }

    @Override
    public IRNode visit(ASTClassDef node) throws BaseError {
        enterASTNode(node);
        var classVars = new ArrayList<IRType>();
        var classFunc = new ArrayList<IRFuncDef>();
        for (var vardef : node.getVars()) {
            currentScope.IRdeclare(vardef.findName());
            classVars.add(new IRType(vardef.getVarType()));
        }
        var constructor = node.getConstructor();
        constructor.getParams().add(0,
                ASTVarDef.builder().info(new VarInfo("this", new TypeInfo(node.findName(), 0))).initexpr(null).build());
        var IRconstructor = (IRFuncDef) constructor.accept(this);
        IRconstructor.setName("class.constructor." + node.findName());
        classFunc.add(IRconstructor);
        for (var func : node.getFuncs()) {
            func.getParams().add(0, ASTVarDef.builder().pos(node.getPos())
                    .info(new VarInfo("this", new TypeInfo(node.findName(), 0))).initexpr(null).build());
            var IRfunc = (IRFuncDef) func.accept(this);
            IRfunc.setName("class.method." + node.findName() + "." + func.findName());
            classFunc.add(IRfunc);
        }
        var classDef = new IRClassDef(classVars, classFunc);
        exitASTNode(node);
        return classDef;
    }

    @Override
    public IRNode visit(ASTVarDef node) throws BaseError {
        enterASTNode(node);
        var varType = new IRVariable(new IRType(node.getVarType()), getVarName(node.findName(), currentScope));
        var instList = new IRStmt();
        currentScope.IRdeclare(node.findName());
        if (node.getInitexpr() != null) {
            var initexpr = ASTAssignExpr.builder().pos(node.getPos())
                    .Info(new ExprInfo("assignExpr", node.getVarType(), false))
                    .left(ASTAtomExpr.builder().pos(node.getPos())
                            .Info(new ExprInfo("atomExpr", node.getVarType(), false))
                            .atomType(ASTAtomExpr.Type.INDENTIFIER).value(node.findName()).constarray(null)
                            .fstring(null).build())
                    .right(node.getInitexpr()).build();
            var initInst = (IRStmt) initexpr.accept(this);
            if (currentScope instanceof GlobalScope) {
                initFunc_add(initInst);
            } else {
                instList.addBlockInsts(initInst);
            }
        }
        if (currentScope instanceof GlobalScope) {
            exitASTNode(node);
            return new IRGlobalDef(varType);
        }
        varType.setType(GlobalScope.irPtrType);
        instList.addFront(new IRAlloca(++InstCounter.InstCounter, varType, new IRType(node.getVarType())));
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTConstarray node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        // var args = node.collectArgs(strDefs);
        // var typename = TypeInfo2Name((TypeInfo) node.getInfo().getType());
        // typename = typename.equals("void") ? "ptr" : typename;
        // var typesize = new IRLiteral(GlobalScope.irIntType,
        // name2Size.get(typename).toString());
        // args.add(0,typesize);
        // var dest = new IRVariable(GlobalScope.irPtrType, "%constarray." +
        // (++counter.allocaCount));
        // instList.addInsts(new IRCall(dest, GlobalScope.irPtrType,
        // "__malloc_const_array", args));
        // instList.setDest(dest);
        // if (node.getDest() == null) {
        // var dest = new IRVariable(GlobalScope.irPtrType, "%constarray." +
        // node.getExpr().size() + "."
        // + node.getDep() + "." + (++counter.constarrayCount));
        // var allocaVar = new IRVariable(GlobalScope.irIntType, "%alloca." +
        // (++counter.allocaCount));
        // var stmts = alloca_unit(GlobalScope.nullType, allocaVar);
        // instList.addBlockInsts(stmts);
        // instList.setDest(dest);
        // node.setDest(dest);
        // }
        IRVariable mallocDest = null;
        if (node.getDest() == null) {
            mallocDest = null;
            instList.setDest(mallocDest);
        } else {
            mallocDest = node.getDest();
        }
        var argnum = node.getExpr().size();
        if (node.getMaze() == 1) {
            var tmpdest = new IRVariable(GlobalScope.irPtrType, "%.tmp.constarray." + node.getExpr().size() + "."
                    + node.getDep() + "." + (++counter.constarrayCount));
            // if (argnum == 0) {
            // var mallocStmt = alloca_unit(GlobalScope.nullType, mallocDest);
            // instList.addBlockInsts(mallocStmt);
            // return instList;
            // }
            if (argnum != 0) {
                var innerType = new TypeInfo(node.getExpr().get(0).getInfo().getType().getName(), 0);
                var info = new ArrayList<IREntity>();
                var arraySize = new IRLiteral(GlobalScope.irIntType, String.valueOf(argnum));
                info.add(arraySize);
                info.add(new IRLiteral(GlobalScope.irIntType, name2Size.get(TypeInfo2Name(innerType)).toString()));
                instList.addInsts(
                        new IRCall(++InstCounter.InstCounter, tmpdest, GlobalScope.irPtrType, "__malloc_array", info));
                if (mallocDest == null) {
                    mallocDest = tmpdest;
                } else {
                    instList.addInsts(new IRStore(++InstCounter.InstCounter, mallocDest, tmpdest));
                }
                for (int i = 0; i < argnum; ++i) {
                    var compute = (IRStmt) node.getExpr().get(i).accept(this);
                    instList.addBlockInsts(compute);
                    var computeDest = compute.getDest();
                    var fetchargs = new ArrayList<IREntity>();
                    var offset = new IRLiteral(GlobalScope.irIntType, String.valueOf(i));
                    fetchargs.add(offset);
                    var dest = new IRVariable(GlobalScope.irPtrType,
                            "%.tmp.constarray." + node.getExpr().size() + "." + node.getDep() + "."
                                    + (++counter.constarrayCount));
                    var destType = new IRType(innerType);
                    var fetchInst = new IRGetelementptr(++InstCounter.InstCounter, dest, destType.typeName, tmpdest,
                            fetchargs);
                    instList.addInsts(fetchInst);
                    instList.addInsts(new IRStore(++InstCounter.InstCounter, dest, computeDest));
                    // instList.addBlockInsts(alloca_unit(innerType, dest));
                }
            }
        } else {
            // have parent
            var info = new ArrayList<IREntity>();
            var arraySize = new IRLiteral(GlobalScope.irIntType, String.valueOf(argnum));
            info.add(arraySize);
            info.add(new IRLiteral(GlobalScope.irIntType, "4"));
            var tmpdest = new IRVariable(GlobalScope.irPtrType, "%.tmp.constarray." + node.getExpr().size() + "."
                    + node.getDep() + "." + (++counter.constarrayCount));
            instList.addInsts(
                    new IRCall(++InstCounter.InstCounter, tmpdest, GlobalScope.irPtrType, "__malloc_array", info));
            if (mallocDest == null) {
                mallocDest = tmpdest;
            } else {
                instList.addInsts(new IRStore(++InstCounter.InstCounter, mallocDest, tmpdest));
            }
            for (int i = 0; i < argnum; ++i) {
                var fetchargs = new ArrayList<IREntity>();
                var offset = new IRLiteral(GlobalScope.irIntType, String.valueOf(i));
                fetchargs.add(offset);
                var dest = new IRVariable(GlobalScope.irPtrType,
                        "%.tmp.constarray." + node.getExpr().size() + "." + node.getDep() + "."
                                + (++counter.constarrayCount));
                var fetchInst = new IRGetelementptr(++InstCounter.InstCounter, dest, GlobalScope.irPtrType.typeName,
                        tmpdest, fetchargs);
                instList.addInsts(fetchInst);
                var constUnit = ((ASTAtomExpr) node.getExpr().get(i)).getConstarray();
                constUnit.setDest(dest);
                instList.addBlockInsts((IRStmt) constUnit.accept(this));
            }
        }
        instList.setDest(mallocDest);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTFstring node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var dest = new IRVariable(GlobalScope.irPtrType, "@str."+fileName+"." + (++IRCounter.strCount));
        var baseStr = new IRStrDef(dest, "");
        strDefs.add(baseStr);
        for (int i = 0; i < node.getStrpart().size(); ++i) {
            var strDest = new IRVariable(GlobalScope.irPtrType, "@str."+fileName+"." + (++IRCounter.strCount));
            var str = new IRStrDef(strDest, node.getStrpart().get(i));
            strDefs.add(str);
            var strAdd = new IRVariable(GlobalScope.irPtrType, "%.tmp.FstringRes." + (++counter.loadCount));
            var args1 = new ArrayList<IREntity>();
            args1.add(dest);
            args1.add(strDest);
            instList.addInsts(
                    new IRCall(++InstCounter.InstCounter, strAdd, GlobalScope.irPtrType, "__string.concat", args1));
            dest = strAdd;
            if (i != node.getStrpart().size() - 1) {
                if (node.getExprpart() != null) {
                    var expr = node.getExprpart().get(i);
                    var exprInst = (IRStmt) expr.accept(this);
                    instList.addBlockInsts(exprInst);
                    var rhsExpr = exprInst.getDest();
                    if (rhsExpr.getType().getTypeName().equals("i1")) {
                        var boolstr = new IRVariable(GlobalScope.irPtrType, "%.tmp.boolstr." + (++counter.loadCount));
                        var callargs = new ArrayList<IREntity>();
                        callargs.add(rhsExpr);
                        var call_bool_string = new IRCall(++InstCounter.InstCounter, boolstr, GlobalScope.irPtrType,
                                "Bool_string.toString", callargs);
                        instList.addInsts(call_bool_string);
                        var args2 = new ArrayList<IREntity>();
                        // var lhsStr2 = new IRVariable(GlobalScope.irPtrType, "%lhs." +
                        // (++counter.loadCount));
                        // instList.addInsts(new IRLoad(lhsStr2, dest));
                        args2.add(dest);
                        args2.add(boolstr);
                        var boolstrAdd = new IRVariable(GlobalScope.irPtrType,
                                "%.tmp.FstringRes." + (++counter.loadCount));
                        instList.addInsts(new IRCall(++InstCounter.InstCounter, boolstrAdd, GlobalScope.irPtrType,
                                "__string.concat", args2));
                        dest = boolstrAdd;
                        // var args2_1 = new ArrayList<IREntity>();
                        // args2_1.add(dest);
                        // args2_1.add(boolstrAdd);
                        // instList.addInsts(new IRCall("__string.copy", args2_1));
                    } else if (rhsExpr.getType().getTypeName().equals("i32")) {
                        var intstr = new IRVariable(GlobalScope.irPtrType, "%.tmp.boolstr." + (++counter.loadCount));
                        var callargs = new ArrayList<IREntity>();
                        callargs.add(rhsExpr);
                        var call_int_string = new IRCall(++InstCounter.InstCounter, intstr, GlobalScope.irPtrType,
                                "toString", callargs);
                        instList.addInsts(call_int_string);
                        var args2 = new ArrayList<IREntity>();
                        // var lhsStr2 = new IRVariable(GlobalScope.irPtrType, "%lhs." +
                        // (++counter.loadCount));
                        // instList.addInsts(new IRLoad(lhsStr2, dest));
                        args2.add(dest);
                        args2.add(intstr);
                        var intstrAdd = new IRVariable(GlobalScope.irPtrType,
                                "%.tmp.FstringRes." + (++counter.loadCount));
                        instList.addInsts(new IRCall(++InstCounter.InstCounter, intstrAdd, GlobalScope.irPtrType,
                                "__string.concat", args2));
                        dest = intstrAdd;
                        // instList.addInsts(new IRStore(dest, intstrAdd));
                        // var args2_1 = new ArrayList<IREntity>();
                        // args2_1.add(dest);
                        // args2_1.add(intstrAdd);
                        // instList.addInsts(new IRCall("__string.copy", args2_1));
                    } else {
                        // var lhsStr2 = new IRVariable(GlobalScope.irPtrType, "%lhs." +
                        // (++counter.loadCount));
                        // instList.addInsts(new IRLoad(lhsStr2, dest));
                        var args2 = new ArrayList<IREntity>();
                        args2.add(dest);
                        args2.add(rhsExpr);
                        var intstrAdd = new IRVariable(GlobalScope.irPtrType,
                                "%.tmp.FstringRes." + (++counter.loadCount));
                        instList.addInsts(new IRCall(++InstCounter.InstCounter, intstrAdd, GlobalScope.irPtrType,
                                "__string.concat", args2));
                        dest = intstrAdd;
                        // var args2_1 = new ArrayList<IREntity>();
                        // args2_1.add(dest);
                        // args2_1.add(intstrAdd);
                        // instList.addInsts(new IRCall("__string.copy", args2_1));
                    }
                }
            }
        }
        instList.setDest(dest);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTNewExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var newtype = node.getType();
        if (newtype.getDepth() == 0) {
            var allocaVar = new IRVariable(GlobalScope.irPtrType, "%.tmp.alloca." + (++counter.allocaCount));
            var stmts = alloca_unit(newtype, allocaVar);
            instList.addBlockInsts(stmts);
            instList.setDest(allocaVar);
        } else {
            // var instList1 = new IRStmt();
            // var args = new ArrayList<IREntity>();
            // for (var sizeinfo : node.getSize()) {
            // var irSizeinfo = (IRStmt) sizeinfo.accept(this);
            // instList1.addBlockInsts(irSizeinfo);
            // args.add(irSizeinfo.getDest());
            // }
            // var typesize = new IRLiteral(GlobalScope.irIntType,
            // name2Size.get(TypeInfo2Name(newtype)).toString());
            // var typedepth = new IRLiteral(GlobalScope.irIntType,
            // String.valueOf(newtype.getDepth()));
            // var typeinited = new IRLiteral(GlobalScope.irIntType,
            // String.valueOf(args.size()));
            // args.add(0, typesize);
            // args.add(1, typedepth);
            // args.add(2, typeinited);
            // var dest = new IRVariable(GlobalScope.irPtrType,
            // "%new." + newtype.getName() + "." + (++counter.allocaCount));
            // var allocaCall = new IRCall(dest, GlobalScope.irPtrType, "__array_alloca",
            // args);
            // instList1.addInsts(allocaCall);
            // instList1.setDest(dest);
            // var instList2 = new IRStmt();
            // if (node.getConstarray() != null) {
            // var constarrayStmts = (IRStmt) node.getConstarray().accept(this);
            // instList2.addBlockInsts(constarrayStmts);
            // instList2.addInsts(new IRStore(dest, constarrayStmts.getDest()));
            // instList2.setDest(dest);
            // }
            // instList.addBlockInsts(instList1);
            // instList.addBlockInsts(instList2);
            // instList.setDest(dest);
            if (node.getSize() != null && node.getSize().size() > 0) {
                var depth = node.getType().getDepth();
                var args = new ArrayList<IREntity>();
                for (var sizeinfo : node.getSize()) {
                    var irSizeinfo = (IRStmt) sizeinfo.accept(this);
                    instList.addBlockInsts(irSizeinfo);
                    args.add(irSizeinfo.getDest());
                }
                var innerType = new TypeInfo(node.getType().getName(), 0);
                IRVariable dest = null;
                // var stmts = alloca_unit(GlobalScope.nullType, dest);
                // instList.addBlockInsts(stmts);
                var initStmt = initArray(args, depth, 0, innerType, dest, loopDepth);
                instList.addBlockInsts(initStmt);
                instList.setDest(initStmt.getDest());
            } else if (node.getConstarray() != null) {
                var constarrayStmts = (IRStmt) node.getConstarray().accept(this);
                instList.addBlockInsts(constarrayStmts);
                instList.setDest(constarrayStmts.getDest());
            }
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTMemberExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var memberInst = (IRStmt) node.getMember().accept(this);
        instList.addBlockInsts(memberInst);
        var ExprInfo = node.getInfo().getType();
        var memberType = node.getMember().getInfo().getDepTypeInfo();
        var caller = (IRVariable) memberInst.getDest();
        if (memberType.getDepth() > 0) {
            instList.setDest(new IRFunc("__builtin_array.size", caller, GlobalScope.irIntType));
        } else if (memberType.equals(GlobalScope.stringType)) {
            if (node.getMemberName().equals("substring")) {
                instList.setDest(new IRFunc("__string.substring", caller, GlobalScope.irPtrType));
            } else {
                instList.setDest(new IRFunc("__string." + node.getMemberName(), caller, GlobalScope.irIntType));
            }
        } else {
            ClassInfo classInfo = null;
            if (node.getMember().getInfo().getDepTypeInfo().equals(GlobalScope.thisType)) {
                ClassScope classScope = currentScope.IRBackSearchClassScope();
                classInfo = (ClassInfo) classScope.getInfo();
            } else {
                classInfo = (ClassInfo) globalScope.containsClasses(memberType.getName());
            }
            if (ExprInfo instanceof FuncInfo) {
                instList.setDest(
                        new IRFunc("class.method." + classInfo.getName() + "." + node.getMemberName(), caller,
                                new IRType(((FuncInfo) ExprInfo).getFunctype())));
            } else {
                var offset = classInfo.getVarOffset(node.getMemberName());
                var destAddr = new IRVariable(GlobalScope.irPtrType,
                        "%.tmp.element." + String.valueOf(++counter.elementCount));
                var args = new ArrayList<IREntity>();
                args.add(new IRLiteral(GlobalScope.irIntType, "0"));
                args.add(new IRLiteral(GlobalScope.irIntType, String.valueOf(offset)));
                instList.addInsts(new IRGetelementptr(++InstCounter.InstCounter, destAddr,
                        "%class." + classInfo.getName(), caller, args));
                var dest = new IRVariable(new IRType(((VarInfo) ExprInfo).getType()),
                        "%.tmp.load." + String.valueOf(++counter.loadCount));
                instList.addInsts(new IRLoad(++InstCounter.InstCounter, dest, destAddr));
                instList.setDest(dest);
                instList.setDestAddr(destAddr);
            }
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTCallExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var args = new ArrayList<IREntity>();
        for (var arg : node.getArgs()) {
            var argInst = (IRStmt) arg.accept(this);
            instList.addBlockInsts(argInst);
            args.add(argInst.getDest());
        }
        var funcInst = (IRStmt) node.getFunc().accept(this);
        instList.addBlockInsts(funcInst);
        var func = (IRFunc) funcInst.getDest();
        if (func.getCaller() != null) {
            args.add(0, func.getCaller());
        }
        var funcRetType = func.getReturnType();
        if (!funcRetType.equals(GlobalScope.irVoidType)) {
            var dest = new IRVariable(funcRetType, "%call." + (++counter.callCount));
            instList.addInsts(new IRCall(++InstCounter.InstCounter, dest, funcRetType, func.getValue(), args));
            instList.setDest(dest);
        } else {
            instList.addInsts(new IRCall(++InstCounter.InstCounter, func.getValue(), args));
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTArrayExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var NameInst = (IRStmt) node.getArrayName().accept(this);
        var IndexInst = (IRStmt) node.getIndex().accept(this);
        instList.addBlockInsts(NameInst);
        instList.addBlockInsts(IndexInst);
        var array = NameInst.getDest();
        var index = IndexInst.getDest();
        var info = new ArrayList<IREntity>();
        info.add(index);
        var destType = new IRType((TypeInfo) node.getInfo().getDepTypeInfo());
        var destAddr = new IRVariable(GlobalScope.irPtrType, "%.tmp.index." + String.valueOf(++counter.loadCount));
        var dest = new IRVariable(destType,
                "%.tmp.load." + String.valueOf(++counter.loadCount));
        var getElemInst = new IRGetelementptr(++InstCounter.InstCounter, destAddr, destType.typeName, array, info);
        instList.addInsts(getElemInst);
        instList.addInsts(new IRLoad(++InstCounter.InstCounter, dest, destAddr));
        instList.setDest(dest);
        instList.setDestAddr(destAddr);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTUnaryExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var exprInst = (IRStmt) node.getExpr().accept(this);
        instList.addBlockInsts(exprInst);
        var former_dest = exprInst.getDest();
        var former_destAddr = (IRVariable) exprInst.getDestAddr();
        var dest = new IRVariable(former_dest.getType(), "%.tmp.unary." + (++counter.arithCount));
        if (node.getOp().equals("++")) {
            instList
                    .addInsts(new IRArith(++InstCounter.InstCounter, dest, "add", GlobalScope.irIntType, former_dest,
                            new IRLiteral(GlobalScope.irIntType, "1")));
        } else if (node.getOp().equals("--")) {
            instList
                    .addInsts(new IRArith(++InstCounter.InstCounter, dest, "sub", GlobalScope.irIntType, former_dest,
                            new IRLiteral(GlobalScope.irIntType, "1")));
        } else {
            throw new IRError("Unknown unary operator");
        }
        instList.addInsts(new IRStore(++InstCounter.InstCounter, former_destAddr, dest));
        instList.setDest(former_dest);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTPreunaryExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var exprInst = (IRStmt) node.getExpr().accept(this);
        instList.addBlockInsts(exprInst);
        var former_dest = exprInst.getDest();
        var former_destAddr = (IRVariable) exprInst.getDestAddr();
        var dest = new IRVariable(former_dest.getType(), "%.tmp.preunary." + (++counter.arithCount));
        if (node.getOp().equals("++")) {
            instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "add", GlobalScope.irIntType, former_dest,
                    new IRLiteral(GlobalScope.irIntType, "1")));
            instList.addInsts(new IRStore(++InstCounter.InstCounter, former_destAddr, dest));
            instList.setDestAddr(former_destAddr);
        } else if (node.getOp().equals("--")) {
            instList
                    .addInsts(new IRArith(++InstCounter.InstCounter, dest, "sub", GlobalScope.irIntType, former_dest,
                            new IRLiteral(GlobalScope.irIntType, "1")));
            instList.addInsts(new IRStore(++InstCounter.InstCounter, former_destAddr, dest));
            instList.setDestAddr(former_destAddr);
        } else if (node.getOp().equals("~")) {
            instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "xor", GlobalScope.irIntType, former_dest,
                    new IRLiteral(GlobalScope.irIntType, "-1")));
        } else if (node.getOp().equals("!")) {
            instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "xor", GlobalScope.irBoolType, former_dest,
                    new IRLiteral(GlobalScope.irBoolType, "true")));
        } else if (node.getOp().equals("-")) {
            instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "sub", GlobalScope.irIntType,
                    new IRLiteral(GlobalScope.irIntType, "0"),
                    former_dest));
        } else {
            throw new IRError("Unknown unary operator");
        }
        instList.setDest(dest);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTBinaryExpr node) throws BaseError {// 如何处理左右出现null的情况
        enterASTNode(node);
        var instList = new IRStmt();
        var lhsInst = (IRStmt) node.getLeft().accept(this);
        var rhsInst = (IRStmt) node.getRight().accept(this);
        IREntity lhs = null;
        IREntity rhs = null;
        var resType = new IRType((TypeInfo) node.getInfo().getType());
        var dest = new IRVariable(resType, "%.tmp.binary." + (++counter.arithCount));
        if (node.getOp().equals("&&") || node.getOp().equals("||")) {
            var rhsDest = rhsInst.getDest();
            var num = IRIf.addCount();
            IRIf stmt;
            if (node.getOp().equals("&&")) {
                stmt = new IRIf(num, lhsInst, rhsInst, null,loopDepth);
            } else {
                stmt = new IRIf(num, lhsInst, null, rhsInst,loopDepth);
            }
            instList.addBlockInsts(stmt);
            var trueLiteral = new IRLiteral(GlobalScope.irBoolType, "true");
            var falseLiteral = new IRLiteral(GlobalScope.irBoolType, "false");
            if (node.getOp().equals("&&")) {
                ArrayList<IREntity> vals = new ArrayList<IREntity>();
                ArrayList<IRLabel> labels = new ArrayList<IRLabel>();
                vals.add(rhsDest);
                labels.add(stmt.getBodyLabel());
                vals.add(falseLiteral);
                labels.add(stmt.getCondLabel());
                instList.addInsts(new IRPhi(++InstCounter.InstCounter, dest, GlobalScope.irBoolType, vals, labels));
            } else {
                ArrayList<IREntity> vals = new ArrayList<IREntity>();
                ArrayList<IRLabel> labels = new ArrayList<IRLabel>();
                vals.add(trueLiteral);
                labels.add(stmt.getCondLabel());
                vals.add(rhsDest);
                labels.add(stmt.getElseLabel());
                instList.addInsts(new IRPhi(++InstCounter.InstCounter, dest, GlobalScope.irBoolType, vals, labels));
            }
            instList.setDest(dest);
            // exitASTNode(node);
            // return instList;
            // instList.setDest(dest);
        } else if (((TypeInfo) node.getLeft().getInfo().getDepTypeInfo()).equals(GlobalScope.stringType)) {
            instList.addBlockInsts(lhsInst);
            instList.addBlockInsts(rhsInst);
            lhs = lhsInst.getDest();
            rhs = rhsInst.getDest();
            var args = new ArrayList<IREntity>();
            args.add(lhs);
            args.add(rhs);
            if (node.getOp().equals("+")) {
                instList.addInsts(new IRCall(++InstCounter.InstCounter, dest, resType, "__string.concat", args));
            } else {
                var Middest = new IRVariable(GlobalScope.irIntType, "%.tmp.mid." + (++counter.arithCount));
                instList.addInsts(new IRCall(++InstCounter.InstCounter, Middest, GlobalScope.irIntType,
                        "__string.compare", args));
                var op = node.getOp();
                if (op.equals("==")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "eq", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else if (op.equals("!=")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "ne", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else if (op.equals("<")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "slt", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else if (op.equals(">")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sgt", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else if (op.equals("<=")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sle", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else if (op.equals(">=")) {
                    instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sge", GlobalScope.irIntType, Middest,
                            new IRLiteral(GlobalScope.irIntType, "0")));
                } else {
                    throw new IRError("Unknown string operator");
                }
            }
            instList.setDest(dest);
        } else {
            instList.addBlockInsts(lhsInst);
            instList.addBlockInsts(rhsInst);
            lhs = lhsInst.getDest();
            rhs = rhsInst.getDest();
            var op = node.getOp();
            if (op.equals("*")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "mul", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("/")) {
                instList.addInsts(
                        new IRArith(++InstCounter.InstCounter, dest, "sdiv", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("%")) {
                instList.addInsts(
                        new IRArith(++InstCounter.InstCounter, dest, "srem", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("+")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "add", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("-")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "sub", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("<<")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "shl", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals(">>")) {
                instList.addInsts(
                        new IRArith(++InstCounter.InstCounter, dest, "ashr", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("&")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "and", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("|")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "or", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("^")) {
                instList.addInsts(new IRArith(++InstCounter.InstCounter, dest, "xor", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("!=")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "ne", lhs.getType(), lhs, rhs));
            } else if (op.equals("==")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "eq", lhs.getType(), lhs, rhs));
            } else if (op.equals("<")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "slt", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals(">")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sgt", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals("<=")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sle", GlobalScope.irIntType, lhs, rhs));
            } else if (op.equals(">=")) {
                instList.addInsts(new IRIcmp(++InstCounter.InstCounter, dest, "sge", GlobalScope.irIntType, lhs, rhs));
            } else {
                throw new IRError("Unknown binary operator");
            }
            instList.setDest(dest);
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTConditionalExpr node) throws BaseError {// 怎么处理左右类型不匹配的情况
        enterASTNode(node);
        var instList = new IRStmt();
        var condInst = (IRStmt) node.getQues().accept(this);
        var trueInst = (IRStmt) node.getLeft().accept(this);
        var falseInst = (IRStmt) node.getRight().accept(this);
        int num = IRIf.addCount();
        if (trueInst.getDest() == null) {
            var ifInsts = new IRIf(num, condInst, trueInst, falseInst,loopDepth);
            instList.addBlockInsts(ifInsts);
        } else {
            var stmt = new IRIf(num, condInst, trueInst, falseInst,loopDepth);
            var dest = new IRVariable(trueInst.getDest().getType(),
                    "%.conditional." + String.valueOf(counter.arithCount++));
            instList.addBlockInsts(stmt);
            ArrayList<IREntity> vals = new ArrayList<IREntity>();
            ArrayList<IRLabel> labels = new ArrayList<IRLabel>();
            vals.add(trueInst.getDest());
            labels.add(stmt.getBodyLabel());
            vals.add(falseInst.getDest());
            labels.add(stmt.getElseLabel());
            instList.addInsts(new IRPhi(++InstCounter.InstCounter, dest, trueInst.getDest().getType(), vals, labels));
            instList.setDest(dest);
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTAssignExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var lhsInst = (IRStmt) node.getLeft().accept(this);
        var rhsInst = (IRStmt) node.getRight().accept(this);
        instList.addBlockInsts(lhsInst);
        instList.addBlockInsts(rhsInst);
        var rhs = rhsInst.getDest();
        var lhsAddr = (IRVariable) lhsInst.getDestAddr();
        if (node.getRight().getInfo().getDepTypeInfo().equals(GlobalScope.stringType)) {
            var args = new ArrayList<IREntity>();
            args.add(lhsAddr);
            args.add(rhs);
            instList.addInsts(new IRCall(++InstCounter.InstCounter, "__string.copy", args));
        } else {
            instList.addInsts(new IRStore(++InstCounter.InstCounter, lhsAddr, rhs));
        }
        instList.setDest(rhs);
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTAtomExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        if (node.getAtomType() == ASTAtomExpr.Type.INDENTIFIER) {
            var info = currentScope.IRBackSearch(node.getValue());
            if (info instanceof VarInfo) {
                BaseScope scope = currentScope.IRBackSearchScope(node.getValue());
                if (scope instanceof ClassScope) {
                    var thisNode = ASTMemberExpr.builder().pos(node.getPos())
                            .Info(new ExprInfo("memberExpr", node.getInfo().getType(), node.getInfo().isLvalue()))
                            .member(ASTAtomExpr.builder().pos(node.getPos())
                                    .Info(new ExprInfo("atomExpr", GlobalScope.thisType, true))
                                    .atomType(ASTAtomExpr.Type.THIS).value("this").constarray(null).fstring(null)
                                    .build())
                            .memberName(node.getValue()).build();
                    var thisInst = (IRStmt) thisNode.accept(this);
                    instList.addBlockInsts(thisInst);
                    instList.setDest(thisInst.getDest());
                    instList.setDestAddr(thisInst.getDestAddr());
                } else {
                    var irtype = new IRType(((VarInfo) info).getType());
                    var src = new IRVariable(GlobalScope.irPtrType, getVarName(node.getValue(), scope));
                    var dest = new IRVariable(irtype, "%.tmp.load." + (++counter.loadCount));
                    instList.addInsts(new IRLoad(++InstCounter.InstCounter, dest, src));
                    instList.setDest(dest);
                    instList.setDestAddr(src);
                }
            } else {
                info = currentScope.BackSearch(node.getValue());
                var closeScope = currentScope.BackSearchScope(node.getValue());
                if (info instanceof FuncInfo) {
                    if (closeScope instanceof GlobalScope) {
                        instList.setDest(
                                new IRFunc(node.getValue(), null, new IRType(((FuncInfo) info).getFunctype())));
                    } else {
                        var thisNode = ASTMemberExpr.builder().pos(node.getPos())
                                .Info(new ExprInfo("memberExpr", node.getInfo().getType(), node.getInfo().isLvalue()))
                                .member(ASTAtomExpr.builder().pos(node.getPos())
                                        .Info(new ExprInfo("atomExpr", GlobalScope.thisType, true))
                                        .atomType(ASTAtomExpr.Type.THIS).value("this").constarray(null).fstring(null)
                                        .build())
                                .memberName(node.getValue()).build();
                        var thisInst = (IRStmt) thisNode.accept(this);
                        instList.addBlockInsts(thisInst);
                        instList.setDest(thisInst.getDest());
                        instList.setDestAddr(thisInst.getDestAddr());
                        // instList.setDest(new IRFunc(
                        // "class.method." + closeScope.getInfo().getName() + "." + node.getValue(),
                        // null,
                        // new IRType(((FuncInfo) info).getFunctype())));
                    }
                } else {
                    throw new IRError("IRBuilder.visit(ASTAtomExpr) should not be called");
                }
            }
        } else if (node.getAtomType() == ASTAtomExpr.Type.INT) {
            instList.setDest(new IRLiteral(GlobalScope.irIntType, node.getValue()));
        } else if (node.getAtomType() == ASTAtomExpr.Type.BOOL) {
            instList.setDest(new IRLiteral(GlobalScope.irBoolType, node.getValue()));
        } else if (node.getAtomType() == ASTAtomExpr.Type.STRING) {
            var dest = new IRVariable(GlobalScope.irPtrType, "@str."+fileName+"." + (++IRCounter.strCount));
            var str = new IRStrDef(dest, node.getValue());
            strDefs.add(str);
            instList.setDest(dest);
        } else if (node.getAtomType() == ASTAtomExpr.Type.NULL) {
            instList.setDest(new IRLiteral(GlobalScope.irPtrType, "null"));
        } else if (node.getAtomType() == ASTAtomExpr.Type.THIS) {
            var src = new IRVariable(GlobalScope.irPtrType, "%this");
            var dest = new IRVariable(GlobalScope.irPtrType, "%.tmp.load." + (++counter.loadCount));
            instList.addInsts(new IRLoad(++InstCounter.InstCounter, dest, src));
            instList.setDest(dest);
            instList.setDestAddr(src);
        } else if (node.getAtomType() == ASTAtomExpr.Type.FSTRING) {
            var FstringInst = (IRStmt) node.getFstring().accept(this);
            instList.addBlockInsts(FstringInst);
            instList.setDest(FstringInst.getDest());
        } else if (node.getAtomType() == ASTAtomExpr.Type.CONSTARRAY) {
            // var irType = new IRType((TypeInfo) node.getConstarray().getInfo().getType());
            // var dest = new IRVariable(irType, "@constarray." +
            // (++counter.constarrayCount));
            var constArrayInst = (IRStmt) node.getConstarray().accept(this);
            instList.addBlockInsts(constArrayInst);
            instList.setDest(constArrayInst.getDest());
            instList.setDestAddr(constArrayInst.getDestAddr());
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTParenExpr node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        var exprInst = (IRStmt) node.getExpr().accept(this);
        instList.addBlockInsts(exprInst);
        instList.setDest(exprInst.getDest());
        if (exprInst.getDestAddr() != null) {
            instList.setDestAddr(exprInst.getDestAddr());
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTBlockstatement node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        for (var stmt : node.getStmts()) {
            var stmtInst = (IRStmt) stmt.accept(this);
            instList.addBlockInsts(stmtInst);
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTBreakstatement node) throws BaseError {
        enterASTNode(node);
        var InstList = new IRStmt();
        InstList.addInsts(new IRBranch(++InstCounter.InstCounter,
                new IRLabel("loop." + String.valueOf(currentScope.LastLoop().getLoopCnt()) + ".endLabel",loopDepth)));
        exitASTNode(node);
        return InstList;
    }

    @Override
    public IRNode visit(ASTContinuestatement node) throws BaseError {
        enterASTNode(node);
        var InstList = new IRStmt();
        InstList.addInsts(new IRBranch(++InstCounter.InstCounter,
                new IRLabel("loop." + String.valueOf(currentScope.LastLoop().getLoopCnt()) + ".updateLabel",loopDepth)));
        exitASTNode(node);
        return InstList;
    }

    @Override
    public IRNode visit(ASTEmptystatement node) throws BaseError {
        enterASTNode(node);
        exitASTNode(node);
        return new IRStmt();
    }

    @Override
    public IRNode visit(ASTExpressionstatement node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        for (var expr : node.getExpr()) {
            var exprInst = (IRStmt) expr.accept(this);
            instList.addBlockInsts(exprInst);
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTForstatement node) throws BaseError {
        enterASTNode(node);
        ++loopDepth;
        var instList = new IRStmt();
        ((LoopScope) node.getScope()).setLoopCnt(IRLoop.addCount());
        IRStmt initStmt = null;
        if (node.getVarinit() != null) {
            initStmt = (IRStmt) node.getVarinit().accept(this);
        } else if (node.getExprinit() != null) {
            initStmt = (IRStmt) node.getExprinit().accept(this);
        }
        var condStmt = node.getCond() == null ? null : (IRStmt) node.getCond().accept(this);
        var updateStmt = node.getStep() == null ? null : (IRStmt) node.getStep().accept(this);
        var bodyStmt = (IRStmt) node.getStmts().accept(this);
        var loop = new IRLoop(((LoopScope) node.getScope()).getLoopCnt(), initStmt, condStmt, updateStmt, bodyStmt,
                loopDepth);
        instList.addBlockInsts(loop);
        --loopDepth;
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTIfstatement node) throws BaseError {
        var instList = new IRStmt();
        var num = IRIf.addCount();
        var condInst = (IRStmt) node.getJudge().accept(this);
        enterASTIfNode(node, "if");
        var bodyInst = (IRStmt) node.getIfstmt().accept(this);
        exitASTIfNode(node, "if");
        if (node.getElsestmt() != null) {
            enterASTIfNode(node, "else");
            var elseInst = (IRStmt) node.getElsestmt().accept(this);
            exitASTIfNode(node, "else");
            instList.addBlockInsts(new IRIf(num, condInst, bodyInst, elseInst,loopDepth));
        } else {
            instList.addBlockInsts(new IRIf(num, condInst, bodyInst, null,loopDepth));
        }
        return instList;
    }

    @Override
    public IRNode visit(ASTReturnstatement node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        if (node.getRet() != null) {
            var exprInst = (IRStmt) node.getRet().accept(this);
            instList.addBlockInsts(exprInst);
            instList.addInsts(new IRRet(++InstCounter.InstCounter, exprInst.getDest()));
        } else {
            instList.addInsts(new IRRet(++InstCounter.InstCounter));
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTVarstatement node) throws BaseError {
        enterASTNode(node);
        var instList = new IRStmt();
        for (var var : node.getVarDefs()) {
            var varInst = (IRStmt) var.accept(this);
            instList.addBlockInsts(varInst);
        }
        exitASTNode(node);
        return instList;
    }

    @Override
    public IRNode visit(ASTWhilestatement node) throws BaseError {
        enterASTNode(node);
        ++loopDepth;
        var instList = new IRStmt();
        ((LoopScope) node.getScope()).setLoopCnt(IRLoop.addCount());
        var condStmt = (IRStmt) node.getJudge().accept(this);
        var bodyStmt = (IRStmt) node.getStmts().accept(this);
        var loop = new IRLoop(((LoopScope) node.getScope()).getLoopCnt(), null, condStmt, null, bodyStmt, loopDepth);
        instList.addBlockInsts(loop);
        --loopDepth;
        exitASTNode(node);
        return instList;
    }
}
