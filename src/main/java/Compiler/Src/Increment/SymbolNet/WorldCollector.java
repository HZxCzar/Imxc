package Compiler.Src.Increment.SymbolNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.AST.Node.DefNode.*;
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
        return msg;
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
        if (!first) {
            ((GlobalScope) astProgram.getGscope()).inheritVar(scope);
        }
        return new WError();
    }

    public ASTRoot GlobalVarCollectRelease(WorldScope scope) throws BaseError,IOException {
        ASTRoot astProgram = new ASTRoot();
        for(ArrayList<ASTVarDef> defSet : scope.getGvars().values()) {
            for(ASTVarDef def : defSet) {
                astProgram.getDefNodes().add(def);
            }
        }
        return astProgram;
    }
}
