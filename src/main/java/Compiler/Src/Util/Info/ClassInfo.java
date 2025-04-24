package Compiler.Src.Util.Info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;

import Compiler.Src.AST.Node.DefNode.ASTVarDef;
import Compiler.Src.AST.Node.DefNode.ASTFuncDef;

@lombok.Getter
@lombok.Setter
public class ClassInfo extends BaseInfo{
    public FuncInfo constructor;
    public HashMap<String, VarInfo> vars;
    public HashMap<String, FuncInfo> funcs;

    public ClassInfo() {
        super("");
        this.constructor = null;
        this.vars = new HashMap<String, VarInfo>();
        this.funcs = new HashMap<String, FuncInfo>();
    }

    public ClassInfo(String name, ASTFuncDef constructor, ArrayList<ASTVarDef> vars, ArrayList<ASTFuncDef> funcs) {
        super(name);
        this.constructor = (FuncInfo) constructor.getInfo();
        this.vars = new HashMap<String, VarInfo>();
        this.funcs = new HashMap<String, FuncInfo>();
        for (ASTVarDef v : vars) {
            this.vars.put(v.findName(), (VarInfo) v.getInfo());
        }
        for (ASTFuncDef func : funcs) {
            this.funcs.put(func.findName(), (FuncInfo) func.getInfo());
        }
    }

    public ClassInfo(String name, FuncInfo... funcs) {
        super(name);
        this.vars = new HashMap<String, VarInfo>();
        this.funcs = new HashMap<String, FuncInfo>();
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

    /**
     * 将本类所有字段写出。注意：必须和 readExternal 严格一一对应。
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1. 写出 BaseInfo.name
        super.writeExternal(out);

        // 2. 写出 constructor（可能为 null）
        out.writeBoolean(constructor != null);
        if (constructor != null) {
            out.writeObject(constructor);
        }

        // 3. 写出 vars map
        out.writeInt(vars.size());
        for (var entry : vars.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }

        // 4. 写出 funcs map
        out.writeInt(funcs.size());
        for (var entry : funcs.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    /**
     * 按 writeExternal 的顺序逐一读回字段
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1. 读 name 并设置
        super.readExternal(in);

        // 2. 读 constructor
        boolean hasCtor = in.readBoolean();
        if (hasCtor) {
            this.constructor = (FuncInfo) in.readObject();
        } else {
            this.constructor = null;
        }

        // 3. 读 vars
        int varCount = in.readInt();
        this.vars = new HashMap<>(varCount);
        for (int i = 0; i < varCount; i++) {
            String varName = in.readUTF();
            VarInfo vinfo  = (VarInfo) in.readObject();
            this.vars.put(varName, vinfo);
        }

        // 4. 读 funcs
        int funcCount = in.readInt();
        this.funcs = new HashMap<>(funcCount);
        for (int i = 0; i < funcCount; i++) {
            String fName  = in.readUTF();
            FuncInfo finfo = (FuncInfo) in.readObject();
            this.funcs.put(fName, finfo);
        }
    }
}
