package Compiler.Src.Util.ScopeUtil;

import Compiler.Src.Util.Info.BaseInfo;
import Compiler.Src.Util.Info.VarInfo;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class FuncScope extends BaseScope {
    private boolean Exit;
    
    public FuncScope(BaseScope parent,BaseInfo info) {
        super(parent, info);
        this.Exit = false;
    }

    @Override
    public void declare(BaseInfo var) {
        if(var instanceof VarInfo)
        {
            vars.put(var.getName(), (VarInfo)var);
        }
        else{
            throw new Error("FuncScope.declare(BaseInfo) should not be called");
        }
    }

    @Override
    public BaseInfo BackSearch(String name)
    {
        if(vars.containsKey(name))
        {
            return vars.get(name);
        }
        else if(this.parent!=null)
        {
            return parent.BackSearch(name);
        }
        return null;
    }

    public boolean isExited() {
        return Exit;
    }
}
