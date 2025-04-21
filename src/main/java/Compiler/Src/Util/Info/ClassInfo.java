package Compiler.Src.Util.Info;

import java.util.ArrayList;
import java.util.TreeMap;

import Compiler.Src.AST.Node.DefNode.ASTVarDef;
import Compiler.Src.AST.Node.DefNode.ASTFuncDef;

@lombok.Getter
@lombok.Setter
public class ClassInfo extends BaseInfo {
    public FuncInfo constructor;
    public TreeMap<String, VarInfo> vars;
    public TreeMap<String, FuncInfo> funcs;

    public ClassInfo(String name, ASTFuncDef constructor, ArrayList<ASTVarDef> vars, ArrayList<ASTFuncDef> funcs) {
        super(name);
        this.constructor = (FuncInfo) constructor.getInfo();
        this.vars = new TreeMap<String, VarInfo>();
        this.funcs = new TreeMap<String, FuncInfo>();
        for (ASTVarDef v : vars) {
            this.vars.put(v.findName(), (VarInfo) v.getInfo());
        }
        for (ASTFuncDef func : funcs) {
            this.funcs.put(func.findName(), (FuncInfo) func.getInfo());
        }
    }

    public ClassInfo(String name, FuncInfo... funcs) {
        super(name);
        this.vars = new TreeMap<String, VarInfo>();
        this.funcs = new TreeMap<String, FuncInfo>();
        for (FuncInfo func : funcs) {
            this.funcs.put(func.getName(), func);
        }
    }

    public BaseInfo get(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else if (funcs.containsKey(name)) {
            return funcs.get(name);
        }
        return null;
    }

    public int getVarOffset(String name) {
        int offset = 0;
        for (String var : vars.keySet()) {
            if (var.equals(name)) {
                return offset;
            }
            offset += 1;
        }
        return -1;
    }
}
