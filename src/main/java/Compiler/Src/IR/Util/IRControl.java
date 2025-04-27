package Compiler.Src.IR.Util;

import Compiler.Src.Util.ScopeUtil.BaseScope;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import Compiler.Src.AST.Node.ASTNode;
import Compiler.Src.AST.Node.StatementNode.ASTIfstatement;
import Compiler.Src.AST.Node.Util.ASTScopedNode;
import Compiler.Src.Util.Error.*;
import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.IR.Node.Def.IRFuncDef;
import Compiler.Src.IR.Node.Def.IRStrDef;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.Stmt.*;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.Type.IRType;

@lombok.Getter
@lombok.Setter
public class IRControl {
    protected GlobalScope globalScope;
    protected BaseScope currentScope;
    protected IRCounter counter;
    protected IRFuncDef initFunc;
    protected final String fileName;
    // public static int InstCounter = 0;
    protected ArrayList<IRStrDef> strDefs;
    public HashMap<String, Integer> name2Size;

    public IRControl(String fileName) {
        this.counter = new IRCounter();
        this.initFunc = new IRFuncDef("main.global.init", new ArrayList<IRVariable>(), GlobalScope.irVoidType,
                new ArrayList<IRBlock>());
        this.initFunc.getBlockstmts().add(new IRBlock(new IRLabel("entry", 0), 0));
        this.strDefs = new ArrayList<IRStrDef>();
        this.name2Size = new HashMap<>();
        this.fileName = fileName;
        name2Size.put("i1", 1);
        name2Size.put("i32", 4);
        name2Size.put("ptr", 4);
    }

    public void enterASTNode(ASTNode node) {
        if (!(node instanceof ASTScopedNode)) {
            return;
        }
        var scope = ((ASTScopedNode) node).getScope();
        if (scope == null) {
            return;
        }
        currentScope = scope;
        if (globalScope == null) {
            if (!(currentScope instanceof GlobalScope)) {
                throw new IRError("Global scope not found");
            }
            globalScope = (GlobalScope) scope;
        }
    }

    public void enterASTIfNode(ASTIfstatement node, String kind) {
        if (kind.equals("if")) {
            currentScope = node.getIfScope();
        } else if (kind.equals("else")) {
            currentScope = node.getElseScope();
        } else {
            throw new IRError("Invalid if stmt node name");
        }
    }

    public void exitASTNode(ASTNode node) {
        if (node instanceof ASTScopedNode) {
            var scope = ((ASTScopedNode) node).getScope();
            if (scope != null) {
                currentScope = scope.getParent();
            }
        }
    }

    public void exitASTIfNode(ASTIfstatement node, String kind) {
        if (kind.equals("if")) {
            var scope = node.getIfScope();
            if (scope != null) {
                currentScope = scope.getParent();
            } else {
                throw new IRError("If scope not found");
            }
        } else if (kind.equals("else")) {
            var scope = node.getElseScope();
            if (scope != null) {
                currentScope = scope.getParent();
            }
        } else {
            throw new IRError("Invalid if stmt node name");
        }
    }

    protected String getVarName(String name, BaseScope scope) {
        if (name.equals("this")) {
            return "%" + name;
        }
        if (scope instanceof GlobalScope) {
            return "@" + name;
        }
        return "%" + name + ".depth." + scope.getScopedep() + ".tags." + scope.GetTagsString();
    }

    public ArrayList<IRBlock> stmt2block(IRStmt stmts, IRType irType) {
        var blocks = new ArrayList<IRBlock>();
        blocks.add(0, new IRBlock(new IRLabel("start",0), 0));
        var enterblock = new IRBlock(new IRLabel("entry",0), 0);
        for (var inst : stmts.getInsts()) {
            if (inst instanceof IRLabel) {
                if (blocks.get(blocks.size() - 1).getReturnInst() == null) {
                    throw new IRError("Block doesn't have return instruction");
                }
                blocks.add(new IRBlock((IRLabel) inst, ((IRLabel) inst).getLoopDepth()));
            } else {
                if (blocks.get(blocks.size() - 1).getReturnInst() != null) {
                    continue;
                }
                if (inst instanceof IRRet || inst instanceof IRBranch) {
                    // blocks.get(blocks.size() - 1).addInsts(inst);
                    blocks.get(blocks.size() - 1).setReturnInst(inst);
                } else if (inst instanceof IRAlloca) {
                    enterblock.addInsts(inst);
                } else if (inst instanceof IRPhi) {
                    blocks.get(blocks.size() - 1).getPhiList().put(((IRPhi) inst).getDest(), (IRPhi) inst);
                } else {
                    blocks.get(blocks.size() - 1).addInsts(inst);
                }
            }
        }
        var lastblock = blocks.get(blocks.size() - 1);
        if (lastblock.getReturnInst() == null) {
            if (irType.equals(GlobalScope.irVoidType)) {
                var ret = new IRRet(++InstCounter.InstCounter);
                // lastblock.addInsts(ret);
                lastblock.setReturnInst(ret);
            } else {
                var branch = new IRBranch(++InstCounter.InstCounter,
                        new IRLabel(lastblock.getLabelName().getLabel(), lastblock.getLabelName().getLoopDepth()));
                // lastblock.addInsts(branch);
                lastblock.setReturnInst(branch);
            }
        }
        // var firstblock = blocks.get(0);
        // enterblock.addBlockInsts(firstblock);
        // enterblock.setReturnInst(firstblock.getReturnInst());
        var enterBranch2start = new IRLabel("start",0);
        // enterblock.addInsts(new IRBranch(enterBranch2start));
        enterblock.setReturnInst(
                new IRBranch(++InstCounter.InstCounter, enterBranch2start));
        blocks.add(0, enterblock);
        return blocks;
    }

    public void initFunc_add(IRStmt stmts) {
        var blocks = initFunc.getBlockstmts();
        var enterblock = initFunc.getBlockstmts().get(0);
        for (var inst : stmts.getInsts()) {
            if (inst instanceof IRLabel) {
                var br = new IRBranch(++InstCounter.InstCounter, (IRLabel) inst);
                if (blocks.get(blocks.size() - 1).getReturnInst() == null) {
                    // blocks.get(blocks.size() - 1).addInsts(inst);
                    blocks.get(blocks.size() - 1).setReturnInst(br);
                }
                blocks.add(new IRBlock((IRLabel) inst, ((IRLabel) inst).getLoopDepth()));
            } else {
                if (blocks.get(blocks.size() - 1).getReturnInst() != null) {
                    continue;
                }
                if (inst instanceof IRRet || inst instanceof IRBranch) {
                    // blocks.get(blocks.size() - 1).addInsts(inst);
                    blocks.get(blocks.size() - 1).setReturnInst(inst);
                } else if (inst instanceof IRAlloca) {
                    enterblock.addFront(inst);
                } else {
                    blocks.get(blocks.size() - 1).addInsts(inst);
                }
            }
        }
    }

    protected String TypeInfo2Name(TypeInfo type) {
        if (type.getDepth() > 0 || type.equals(GlobalScope.stringType)) {
            return "ptr";
        } else if (type.equals(GlobalScope.intType)) {
            return "i32";
        } else if (type.equals(GlobalScope.boolType)) {
            return "i1";
        } else if (type.equals(GlobalScope.voidType)) {
            return "void";
        } else if (!type.isDefined()) {
            return "%class." + type.getName();
        } else {
            throw new IRError("Invalid type info in Typeinfo2Name");
        }
    }

    protected IRStmt alloca_unit(TypeInfo type, IRVariable allocaVar) {
        if (type.getDepth() > 0) {
            throw new IRError("Invalid alloca unit type");
        }
        var instList = new IRStmt();
        // var allocaVar = new IRVariable(GlobalScope.irIntType, "%alloca." +
        // (++counter.allocaCount));
        var args = new ArrayList<IREntity>();
        if (type.equals(GlobalScope.nullType)) {
            args.add(new IRLiteral(GlobalScope.irIntType, "4"));
        } else {
            // System.out.println("alloca unit type: " + type.getName());
            args.add(new IRLiteral(GlobalScope.irIntType, name2Size.get(TypeInfo2Name(type)).toString()));
        }
        var allocaCall = new IRCall(++InstCounter.InstCounter, allocaVar, GlobalScope.irPtrType, "_malloc", args);
        instList.addInsts(allocaCall);
        if (!type.isDefined()) {
            var constructorargs = new ArrayList<IREntity>();
            constructorargs.add(allocaVar);
            var constructorCall = new IRCall(++InstCounter.InstCounter, "class.constructor." + type.getName(),
                    constructorargs);
            instList.addInsts(constructorCall);
        }
        instList.setDest(allocaVar);
        return instList;
    }

    protected void initSize(String name, ArrayList<IRType> types) {
        var totalSize = 0;
        for (var type : types) {
            totalSize += name2Size.get(type.getTypeName());
        }
        name2Size.put(name, totalSize);
    }

    protected IRStmt initArray(ArrayList<IREntity> args, int full_length, int depth, TypeInfo innerType,
            IRVariable mallocDest, int loopDepth) {
        var stmts = new IRStmt();
        var init = new IRStmt();
        var cond = new IRStmt();
        var update = new IRStmt();
        var body = new IRStmt();
        // var mallocDest = new IRVariable(GlobalScope.irPtrType,
        // "%initArray.mallocDest." + depth + (++counter.ArrayCount));
        if (depth != args.size() - 1) {
            ++loopDepth;
            var info = new ArrayList<IREntity>();
            info.add(args.get(depth));
            info.add(new IRLiteral(GlobalScope.irIntType, "4"));
            var tmpdest = new IRVariable(GlobalScope.irPtrType,
                    "%.tmp.initArray.tmpdest." + depth + (++counter.ArrayCount));
            init.addInsts(
                    new IRCall(++InstCounter.InstCounter, tmpdest, GlobalScope.irPtrType, "__malloc_array", info));
            if (mallocDest != null) {
                init.addInsts(new IRStore(++InstCounter.InstCounter, mallocDest, tmpdest));
            } else {
                mallocDest = tmpdest;
            }
            var initVar = new IRVariable(GlobalScope.irPtrType, "%initArray.tmp." + depth + (++counter.ArrayCount));
            var compTarg = args.get(depth);
            init.addInsts(new IRAlloca(++InstCounter.InstCounter, initVar, GlobalScope.irIntType));
            // var initbeg = new IRLiteral(GlobalScope.irIntType, "0");
            // var cmpTarg = new IRVariable(GlobalScope.irIntType,
            // "%initArray.midArray." + depth + (++counter.ArrayCount));
            // var initmidVar= new IRVariable(GlobalScope.irIntType, "%initArray.midArray."
            // + depth + (++counter.ArrayCount));
            init.addInsts(new IRStore(++InstCounter.InstCounter, initVar, new IRLiteral(GlobalScope.irIntType, "0")));
            // init.addInsts(new IRLoad(cmpTarg, args.get(depth)));
            var condDest = new IRVariable(GlobalScope.irBoolType, "%.tmp.cond." + depth + (++counter.ArrayCount));
            var condmidVar = new IRVariable(GlobalScope.irIntType,
                    "%.tmp.cond.midArray." + depth + (++counter.ArrayCount));
            cond.addInsts(new IRLoad(++InstCounter.InstCounter, condmidVar, initVar));
            cond.addInsts(new IRIcmp(++InstCounter.InstCounter, condDest, "slt", GlobalScope.irBoolType, condmidVar,
                    compTarg));
            cond.setDest(condDest);
            var updatemidVar = new IRVariable(GlobalScope.irIntType,
                    "%.tmp.initArray.update." + depth + (++counter.ArrayCount));
            var updatemidVar2 = new IRVariable(GlobalScope.irIntType,
                    "%.tmp.initArray.update2." + depth + (++counter.ArrayCount));
            update.addInsts(new IRLoad(++InstCounter.InstCounter, updatemidVar, initVar));
            update.addInsts(
                    new IRArith(++InstCounter.InstCounter, updatemidVar2, "add", GlobalScope.irIntType, updatemidVar,
                            new IRLiteral(GlobalScope.irIntType, "1")));
            update.addInsts(new IRStore(++InstCounter.InstCounter, initVar, updatemidVar2));
            var fetchDest = new IRVariable(GlobalScope.irPtrType,
                    "%.tmp.initArray.fetchDest." + depth + (++counter.ArrayCount));
            var offset = new IRVariable(GlobalScope.irIntType,
                    "%.tmp.initArray.offset." + depth + (++counter.ArrayCount));
            body.addInsts(new IRLoad(++InstCounter.InstCounter, offset, initVar));
            var fetchargs = new ArrayList<IREntity>();
            fetchargs.add(offset);
            var fetch = new IRGetelementptr(++InstCounter.InstCounter, fetchDest, GlobalScope.irPtrType.typeName,
                    tmpdest, fetchargs);
            body.addInsts(fetch);
            body.addBlockInsts(initArray(args, full_length, depth + 1, innerType, fetchDest, loopDepth));
            var LoopNode = new IRLoop(IRLoop.addCount(), init, cond, update, body, loopDepth);// change
            stmts.addBlockInsts(LoopNode);
            --loopDepth;
        } else {
            var tmpdest = new IRVariable(GlobalScope.irPtrType,
                    "%.tmp.initArray.tmpdest." + depth + (++counter.ArrayCount));
            if (depth < full_length - 1) {
                // 未完全定义
                var info = new ArrayList<IREntity>();
                info.add(args.get(depth));
                info.add(new IRLiteral(GlobalScope.irIntType, "4"));
                stmts.addInsts(
                        new IRCall(++InstCounter.InstCounter, tmpdest, GlobalScope.irPtrType, "__malloc_array", info));
                // stmts.addBlockInsts(alloca_unit(GlobalScope.nullType, tmpdest));
            } else {
                // 完全定义
                var info = new ArrayList<IREntity>();
                info.add(args.get(depth));
                info.add(new IRLiteral(GlobalScope.irIntType, name2Size.get(TypeInfo2Name(innerType)).toString()));
                stmts.addInsts(
                        new IRCall(++InstCounter.InstCounter, tmpdest, GlobalScope.irPtrType, "__malloc_array", info));
                // stmts.addBlockInsts(alloca_unit(innerType, tmpdest));
            }
            if (mallocDest == null) {
                mallocDest = tmpdest;
            } else {
                stmts.addInsts(new IRStore(++InstCounter.InstCounter, mallocDest, tmpdest));
            }
        }
        stmts.setDest(mallocDest);
        return stmts;
    }
}
