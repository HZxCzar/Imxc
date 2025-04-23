package Compiler.Src.Increment.SymbolNet;

import java.util.HashSet;

import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.AST.Node.DefNode.*;
import Compiler.Src.Increment.Util.Error.WError;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.ScopeUtil.*;
import Compiler.Src.Semantic.*;

public class WorldCollector{
    public WError scan(ASTRoot astProgram,WorldScope scope,String filePath) throws BaseError {
        WError msg = new WError();
        new SymbolCollector().visit(astProgram);
        GlobalVarCollect(astProgram, scope);
        scope.collect(astProgram.getGscope(), filePath);
        return msg;
    }

    public WError GlobalVarCollect(ASTRoot astProgram,WorldScope scope) throws BaseError {
        HashSet<ASTVarDef> rmvSet = new HashSet<ASTVarDef>();
        for (ASTDef def : astProgram.getDefNodes()) {
            if (def instanceof ASTVarDef) {
                scope.declare((VarInfo)def.getInfo());
                scope.getGvars().add((ASTVarDef)def);
                rmvSet.add((ASTVarDef)def);
            }
        }
        for(ASTVarDef def:rmvSet)
        {
            astProgram.getDefNodes().remove(def);
        }
        return new WError();
    }

    public WError inherit(ASTRoot astProgram,WorldScope scope,Boolean first) throws BaseError {
        ((GlobalScope)astProgram.getGscope()).inherit(scope);
        if(!first)
        {
            ((GlobalScope)astProgram.getGscope()).inheritVar(scope);
        }
        return new WError();
    }

    public WError GlobalVarCollectRelease(ASTRoot astProgram,WorldScope scope) throws BaseError {
        for (ASTVarDef def : scope.getGvars()) {
            astProgram.addDef(def,0);
        }
        return new WError();
    }
}
