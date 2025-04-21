package Compiler.Src.Util.ScopeUtil;

import Compiler.Src.Util.Info.*;
import Compiler.Src.Increment.SymbolNet.WorldScope;
import Compiler.Src.Util.*;

import java.util.TreeMap;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class GlobalScope extends BaseScope implements BasicType {
    private TreeMap<String, FuncInfo> funcs;
    private TreeMap<String, ClassInfo> classes;

    public GlobalScope() {
        super(null, null);
        this.funcs = new TreeMap<String, FuncInfo>();
        this.classes = new TreeMap<String, ClassInfo>();
        for(FuncInfo func:BaseFuncs)
        {
            this.funcs.put(func.getName(), func);
        }
        for(ClassInfo cls:BaseClasses)
        {
            this.classes.put(cls.getName(), cls);
        }
    }

    public void inherit(WorldScope scope) {
        for (String name : scope.getFuncs().keySet()) {
            if (!this.funcs.containsKey(name)) {
                this.funcs.put(name, scope.getFuncs().get(name));
            }
        }
        for (String name : scope.getClasses().keySet()) {
            if (!this.classes.containsKey(name)) {
                this.classes.put(name, scope.getClasses().get(name));
            }
        }
    }

    public void inheritVar(WorldScope scope) {
        for (String name : scope.getVars().keySet()) {
            if (!this.vars.containsKey(name)) {
                this.vars.put(name, scope.getVars().get(name));
            }
            if (!this.IRvars.containsKey(name)) {
                this.IRvars.put(name, scope.getVars().get(name));
            }
        }
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
