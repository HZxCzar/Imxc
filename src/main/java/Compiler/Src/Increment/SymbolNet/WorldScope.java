package Compiler.Src.Increment.SymbolNet;

import Compiler.Src.Util.Info.*;
import Compiler.Src.AST.Node.ASTRoot;
import Compiler.Src.Increment.Util.Error.WError;
import Compiler.Src.Util.*;
import Compiler.Src.Util.ScopeUtil.*;

import java.util.HashMap;
import java.util.HashSet;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class WorldScope extends BaseScope implements BasicType {
    private HashMap<String, FuncInfo> funcs;
    private HashMap<String, ClassInfo> classes;
    private HashMap<String, HashSet<String>> func2use;
    private HashMap<String, HashSet<String>> class2use;
    private HashSet<String> basefunc;
    private HashSet<String> baseclass;

    public WorldScope() {
        super(null, null);
        this.funcs = new HashMap<String, FuncInfo>();
        this.classes = new HashMap<String, ClassInfo>();
        this.func2use = new HashMap<String, HashSet<String>>();
        this.class2use = new HashMap<String, HashSet<String>>();
        basefunc = new HashSet<String>();
        baseclass = new HashSet<String>();
        for(FuncInfo func:BaseFuncs)
        {
            this.funcs.put(func.getName(), func);
            basefunc.add(func.getName());
        }
        for(ClassInfo cls:BaseClasses)
        {
            this.classes.put(cls.getName(), cls);
            baseclass.add(cls.getName());
        }
    }

    public WError collect(BaseScope scope,String filePath) {
        if(scope instanceof GlobalScope) {
            GlobalScope gscope = (GlobalScope) scope;
            for (var func : gscope.getFuncs().keySet()) {
                if(basefunc.contains(func))
                {
                    continue;
                }
                if (!funcs.containsKey(func)) {
                    funcs.put(func, gscope.getFuncs().get(func));
                }
                else{
                    throw new WError("func Multi-define: "+func+" in "+filePath);
                }
                if (!func2use.containsKey(func)) {
                    func2use.put(func, new HashSet<String>());
                }
                func2use.get(func).add(filePath);
            }
            for (var cls : gscope.getClasses().keySet()) {
                if(baseclass.contains(cls))
                {
                    continue;
                }
                if (!classes.containsKey(cls)) {
                    classes.put(cls, gscope.getClasses().get(cls));
                }
                else{
                    throw new WError("class Multi-define: "+cls+" in "+filePath);
                }
                if (!class2use.containsKey(cls)) {
                    class2use.put(cls, new HashSet<String>());
                }
                class2use.get(cls).add(filePath);
            }
        } else {
            throw new WError("WorldScope.collect(BaseScope) should not be called");
        }
        return new WError();
    }

    @Override
    public void declare(BaseInfo var) {
        if (var instanceof VarInfo) {
            vars.put(var.getName(), (VarInfo) var);
        } else if (var instanceof FuncInfo) {
            funcs.put(var.getName(), (FuncInfo) var);
        } else if (var instanceof ClassInfo) {
            classes.put(var.getName(), (ClassInfo) var);
        } else {
            throw new Error("GlobalScope.declare(BaseInfo) should not be called");
        }
    }



    @Override
    public boolean contains(String name) {
        if (vars.containsKey(name)) {
            return true;
        } else if (funcs.containsKey(name)) {
            return true;
        } else if (classes.containsKey(name)) {
            return true;
        }
        return false;
    }

    @Override
    public FuncInfo containsFuncs(String name) {
        if(funcs.containsKey(name))
        {
            return funcs.get(name);
        }
        else{
            return null;
        }
    }

    @Override
    public ClassInfo containsClasses(String name) {
        if(classes.containsKey(name))
        {
            return classes.get(name);
        }
        else{
            return null;
        }
    }

    @Override
    public BaseInfo BackSearch(String name)
    {
        if(vars.containsKey(name))
        {
            return vars.get(name);
        }
        else if(funcs.containsKey(name))
        {
            return funcs.get(name);
        }
        else if(classes.containsKey(name))
        {
            return classes.get(name);
        }
        else if(this.parent!=null)
        {
            return parent.BackSearch(name);
        }
        return null;
    }

    @Override
    public BaseScope BackSearchScope(String name)
    {
        if(vars.containsKey(name))
        {
            return this;
        }
        else if(funcs.containsKey(name))
        {
            return this;
        }
        else if(classes.containsKey(name))
        {
            return this;
        }
        else if(this.parent!=null)
        {
            return parent.BackSearchScope(name);
        }
        return null;
    }
}
