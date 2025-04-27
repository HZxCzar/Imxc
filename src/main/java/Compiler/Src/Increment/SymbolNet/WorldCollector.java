package Compiler.Src.Increment.SymbolNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.Def.IRGlobalDef;
import Compiler.Src.IR.Type.IRStructType;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Increment.Util.Error.WError;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.Semantic.*;

public class WorldCollector {
    public WError scan(ASTRoot astProgram, WorldScope scope, String filePath) throws BaseError {
        WError msg = new WError();
        new SymbolCollector().visit(astProgram);
        GlobalVarCollect(astProgram, scope, filePath);
        scope.collect(astProgram.getGscope(), filePath);
        collectSize(astProgram, filePath, scope);
        return msg;
    }

    public WError consistRela(ASTRoot astProgram, WorldScope scope,String File) throws BaseError {
        GlobalScope gscope = (GlobalScope) astProgram.getGscope();
        // clear the related file
        for(var filename:scope.getFile2related().keySet()) {
            for(var name:scope.getFile2related().get(filename)) {
                if(name.equals(File)) {
                    scope.getFile2related().get(filename).remove(File);
                    break;
                }
            }
        }
        // System.out.println("File: " + File);
        for(var filename:scope.getFile2name().keySet()) {
            if(filename.equals(File)) continue;
            for(var name:scope.getFile2name().get(filename)) {
                if(gscope.getFuncNames().contains(name)) {
                    if(scope.getFile2related().get(filename) == null) {
                        scope.getFile2related().put(filename, new HashSet<String>());
                    }
                    scope.getFile2related().get(filename).add(File);
                    // System.out.println("Related File: " + filename + " FuncName: " + name);
                    // break;
                }
                if(gscope.getClassNames().contains(name)) {
                    if(scope.getFile2related().get(filename) == null) {
                        scope.getFile2related().put(filename, new HashSet<String>());
                    }
                    scope.getFile2related().get(filename).add(File);
                    // System.out.println("Related File: " + filename + " ClassName: " + name);
                    // break;
                }
                if(gscope.getVarNames().contains(name)) {
                    if(scope.getFile2related().get(filename) == null) {
                        scope.getFile2related().put(filename, new HashSet<String>());
                    }
                    scope.getFile2related().get(filename).add(File);
                    // System.out.println("Related File: " + filename + " VarName: " + name);
                    // break;
                }
            }
        }
        return new WError();
    }

    public WError collectSize(ASTRoot node, String filePath, WorldScope scope) throws BaseError {
        scope.getFile2gcls().put(filePath, new HashSet<IRGlobalDef>());
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

                var totalSize = 0;
                for (var type : defs) {
                    totalSize += scope.getName2Size().get(type.getTypeName());
                }
                scope.getName2Size().put(typename, totalSize);

                var structtype = new IRStructType(typename, defs, aline);
                structtype.setSize(scope.getName2Size().get(typename));
                scope.getFile2gcls().get(filePath).add(new IRGlobalDef(new IRVariable(structtype, typename)));
            }
        }
        // System.out.println("file: " + filePath + " GlobalDefNum" + scope.getFile2gcls().get(filePath).size());
        return new WError();
    }

    public WError GlobalVarCollect(ASTRoot astProgram, WorldScope scope, String filePath) throws BaseError {
        HashSet<ASTVarDef> rmvSet = new HashSet<ASTVarDef>();
        scope.getGvars().put(filePath, new ArrayList<ASTVarDef>());
        scope.getFile2name().put(filePath, new HashSet<String>());
        for (ASTDef def : astProgram.getDefNodes()) {
            scope.getFile2name().get(filePath).add(def.findName());
            if (def instanceof ASTVarDef) {
                scope.declare((VarInfo) def.getInfo());
                scope.getGvars().get(filePath).add((ASTVarDef) def);
                rmvSet.add((ASTVarDef) def);
            }
        }
        for (ASTVarDef def : rmvSet) {
            astProgram.getDefNodes().remove(def);
        }
        return new WError();
    }

    public WError inherit(ASTRoot astProgram, WorldScope scope, Boolean first) throws BaseError {
        ((GlobalScope) astProgram.getGscope()).inherit(scope);
        // //打印name2size
        // for (var entry : scope.getName2Size().entrySet()) {
        //     System.out.println("name2size: " + entry.getKey() + " " + entry.getValue());
        // }
        astProgram.setName2Size(scope.getName2Size());
        if (!first) {
            ((GlobalScope) astProgram.getGscope()).inheritVar(scope);
        }
        return new WError();
    }

    public ASTRoot GlobalVarCollectRelease(WorldScope scope) throws BaseError, IOException {
        ASTRoot astProgram = new ASTRoot();
        for (ArrayList<ASTVarDef> defSet : scope.getGvars().values()) {
            for (ASTVarDef def : defSet) {
                astProgram.getDefNodes().add(def);
            }
        }
        return astProgram;
    }
}
