package Compiler.Src.Util.ScopeUtil;

import java.util.ArrayList;
import java.util.TreeMap;

import Compiler.Src.Util.Info.*;
import Compiler.Src.Util.Error.*;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class BaseScope {
    protected BaseScope parent;
    protected BaseInfo info;
    protected int scopedep;
    protected int num;
    protected int childnum;
    protected ArrayList<Integer> tags;
    protected TreeMap<String, VarInfo> vars;
    protected TreeMap<String, VarInfo> IRvars;

    public BaseScope(BaseScope parent, BaseInfo info) {
        this.parent = parent;
        this.info = info;
        this.vars = new TreeMap<String, VarInfo>();
        this.IRvars = new TreeMap<String, VarInfo>();
        this.childnum = 0;
        this.tags=new ArrayList<Integer>();
        if(parent!=null){
            this.num=++parent.childnum;
            this.scopedep=parent.scopedep+1;
            for(var tag:parent.tags)
            {
                tags.add(tag);
            }
        }
        else{
            this.scopedep=0;
            this.num=0;
        }
        tags.add(this.num);
    }

    public String GetTagsString()
    {
        String str="";
        for(int i=0;i<tags.size();++i)
        {
            str+=tags.get(i);
            if(i!=tags.size()-1)
            {
                str+=".";
            }
        }
        return str;
    }

    public LoopScope LastLoop()
    {
        BaseScope scope=this;
        if(scope instanceof LoopScope)
        {
            return (LoopScope)scope;
        }
        else if(scope.parent!=null)
        {
            return scope.parent.LastLoop();
        }
        return null;
    }

    public boolean contains(String name) {
        return vars.containsKey(name);
    }

    public boolean IRcontains(String name) {
        return IRvars.containsKey(name);
    }

    public void IRdeclare(String name) {
        if (vars.containsKey(name)) {
            IRvars.put(name, vars.get(name));
        } else {
            throw new Error("BaseScope.declare(IRInfo) should not be called");
        }
    }

    // public void arrayInitDeclare(String name, VarInfo var) {
    //     IRvars.put(name, var);
    // }

    public void declare(BaseInfo var) {
        if (var instanceof VarInfo) {
            vars.put(var.getName(), (VarInfo) var);
        } else {
            throw new Error("BaseScope.declare(BaseInfo) should not be called");
        }
    }

    public FuncInfo containsFuncs(String name) throws ScopeError {
        throw new ScopeError("no containsFuncs");
        // return null;
    }

    public ClassInfo containsClasses(String name) throws ScopeError {
        throw new ScopeError("no containsClasses");
        // return null;
    }

    public VarInfo containsVars(String name) throws ScopeError {
        if (vars.containsKey(name)) {
            return vars.get(name);
        }
        return null;
    }

    public BaseInfo BackSearch(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else if (this.parent != null) {
            return parent.BackSearch(name);
        }
        return null;
    }

    public BaseScope BackSearchScope(String name) {
        if (vars.containsKey(name)) {
            return this;
        } else if (this.parent != null) {
            return parent.BackSearchScope(name);
        }
        return null;
    }

    public BaseInfo IRBackSearch(String name) {
        if (IRvars.containsKey(name)) {
            return IRvars.get(name);
        } else if (this.parent != null) {
            return parent.IRBackSearch(name);
        }
        return null;
    }

    public BaseScope IRBackSearchScope(String name) {
        if (IRvars.containsKey(name)) {
            return this;
        } else if (this.parent != null) {
            return parent.IRBackSearchScope(name);
        }
        return null;
    }

    public FuncScope IRBackSearchFuncScope() {
        if (this instanceof FuncScope) {
            return (FuncScope)this;
        } else if (this.parent != null) {
            return parent.IRBackSearchFuncScope();
        }
        return null;
    }

    public ClassScope IRBackSearchClassScope() {
        if (this instanceof ClassScope) {
            return (ClassScope)this;
        } else if (this.parent != null) {
            return parent.IRBackSearchClassScope();
        }
        return null;
    }
}