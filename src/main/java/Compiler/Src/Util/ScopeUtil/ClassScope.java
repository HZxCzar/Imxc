package Compiler.Src.Util.ScopeUtil;

import Compiler.Src.Util.Info.BaseInfo;
import Compiler.Src.Util.Info.FuncInfo;
import Compiler.Src.Util.Info.VarInfo;

import java.util.TreeMap;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ClassScope extends BaseScope {
    private TreeMap<String, FuncInfo> funcs;

    public ClassScope(BaseScope parent, BaseInfo info) {
        super(parent, info);
        this.funcs = new TreeMap<String, FuncInfo>();
    }

    @Override
    public void declare(BaseInfo var) {
        if (var instanceof VarInfo) {
            vars.put(var.getName(), (VarInfo) var);
        } else if (var instanceof FuncInfo) {
            funcs.put(var.getName(), (FuncInfo) var);
        } else {
            throw new Error("ClassScope.declare(BaseInfo) should not be called");
        }
    }

    @Override
    public boolean contains(String name) {
        if (vars.containsKey(name)) {
            return true;
        } else if (funcs.containsKey(name)) {
            return true;
        }
        return false;
    }

    @Override
    public FuncInfo containsFuncs(String name) {
        if (funcs.containsKey(name)) {
            return funcs.get(name);
        } else {
            return null;
        }
    }

    @Override
    public BaseInfo BackSearch(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else if (funcs.containsKey(name)) {
            return funcs.get(name);
        } else if (this.parent != null) {
            return parent.BackSearch(name);
        }
        return null;
    }

    @Override
    public BaseScope BackSearchScope(String name) {
        if (vars.containsKey(name)) {
            return this;
        } else if (funcs.containsKey(name)) {
            return this;
        } else if (this.parent != null) {
            return parent.BackSearchScope(name);
        }
        return null;
    }

    @Override
    public BaseInfo IRBackSearch(String name) {
        if (IRvars.containsKey(name)) {
            return IRvars.get(name);
        } else if (funcs.containsKey(name)) {
            return funcs.get(name);
        } else if (this.parent != null) {
            return parent.IRBackSearch(name);
        }
        return null;
    }
}
