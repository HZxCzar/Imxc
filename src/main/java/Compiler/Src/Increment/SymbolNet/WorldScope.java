package Compiler.Src.Increment.SymbolNet;

import Compiler.Src.Util.Info.*;
import Compiler.Src.AST.Node.DefNode.ASTVarDef;
import Compiler.Src.IR.Node.Def.IRGlobalDef;
import Compiler.Src.Increment.Util.Error.WError;
import Compiler.Src.Util.*;
import Compiler.Src.Util.ScopeUtil.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class WorldScope implements BasicType, Externalizable {
    private HashMap<String, FuncInfo> funcs;// func info
    private HashMap<String, ClassInfo> classes;// class info
    private HashMap<String, VarInfo> vars;// var info
    private HashMap<String, Integer> name2Size;// class name -> size
    private HashMap<String, ArrayList<ASTVarDef>> Gvars;// global var def
    private HashMap<String, HashSet<String>> file2name;
    private HashMap<String, HashSet<IRGlobalDef>> file2gcls;// file path -> ir global class name
    private HashMap<String, HashSet<String>> file2related;// file path -> related file path
    private HashSet<String> basefunc;
    private HashSet<String> baseclass;

    public WorldScope() {
        this.funcs = new HashMap<String, FuncInfo>();
        this.classes = new HashMap<String, ClassInfo>();
        this.vars = new HashMap<String, VarInfo>();
        this.name2Size = new HashMap<String, Integer>();
        this.Gvars = new HashMap<String, ArrayList<ASTVarDef>>();
        this.file2name = new HashMap<String, HashSet<String>>();
        this.file2gcls = new HashMap<String, HashSet<IRGlobalDef>>();
        this.file2related = new HashMap<String, HashSet<String>>();
        basefunc = new HashSet<String>();
        baseclass = new HashSet<String>();
        for (FuncInfo func : BaseFuncs) {
            this.funcs.put(func.getName(), func);
            basefunc.add(func.getName());
        }
        for (ClassInfo cls : BaseClasses) {
            this.classes.put(cls.getName(), cls);
            baseclass.add(cls.getName());
        }
    }

    public void filter(List<String> toRecompile, List<String> toSkip) {
        // 1) 构造一个只包含 toSkip 的路径字符串集合
        Set<String> skipSet = toSkip.stream()
                .map(path -> path.toString())
                .collect(Collectors.toSet());

        // 2) 直接操作 keySet，删掉那些不在 skipSet 里的
        file2name.keySet().removeIf(pathStr -> !skipSet.contains(pathStr));
        Gvars.keySet().removeIf(pathStr -> !skipSet.contains(pathStr));
        file2gcls.keySet().removeIf(pathStr -> !skipSet.contains(pathStr));
        // 打印file2name
        // for (var entry : file2name.entrySet()) {
        //     System.out.println("file2name: " + entry.getKey() + " -> " + entry.getValue());
        // }
        Set<String> keepNames = file2name.values()
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        funcs.keySet().removeIf(funcName -> !keepNames.contains(funcName));
        classes.keySet().removeIf(className -> !keepNames.contains(className));
        vars.keySet().removeIf(varName -> !keepNames.contains(varName));
        name2Size.keySet().removeIf(name -> {
            String key = name.startsWith("%class.") ? name.substring(7) : name;
            return !keepNames.contains(key);
        });
        name2Size.put("i1", 1);
        name2Size.put("i32", 4);
        name2Size.put("ptr", 4);
        System.out.println("WorldScope.filter: " + file2name.size() + " files, " +
                funcs.size() + " funcs, " + classes.size() + " classes, " +
                vars.size() + " vars, " + Gvars.size() + " Gvars");
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 1) funcs
        out.writeInt(funcs.size());
        for (Map.Entry<String, FuncInfo> e : funcs.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeObject(e.getValue());
        }
        // 2) classes
        out.writeInt(classes.size());
        for (Map.Entry<String, ClassInfo> e : classes.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeObject(e.getValue());
        }
        // 3) vars
        out.writeInt(vars.size());
        for (Map.Entry<String, VarInfo> e : vars.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeObject(e.getValue());
        }
        // 4) name2Size
        out.writeInt(name2Size.size());
        for (Map.Entry<String, Integer> e : name2Size.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        // 5) 全局变量定义 Gvars
        out.writeInt(Gvars.size());
        for (Map.Entry<String, ArrayList<ASTVarDef>> e : Gvars.entrySet()) {
            // 写入文件路径
            out.writeUTF(e.getKey());
            // 写入该文件下全局变量定义的数量
            ArrayList<ASTVarDef> defs = e.getValue();
            out.writeInt(defs.size());
            // 写入每个 ASTVarDef 对象
            for (ASTVarDef def : defs) {
                out.writeObject(def);
            }
        }
        // 6) file2name
        out.writeInt(file2name.size());
        for (Map.Entry<String, HashSet<String>> e : file2name.entrySet()) {
            out.writeUTF(e.getKey());
            HashSet<String> names = e.getValue();
            out.writeInt(names.size());
            for (String name : names) {
                out.writeUTF(name);
            }
        }
        // 7) file2gcls
        out.writeInt(file2gcls.size());
        for (Map.Entry<String, HashSet<IRGlobalDef>> e : file2gcls.entrySet()) {
            out.writeUTF(e.getKey());
            HashSet<IRGlobalDef> gcls = e.getValue();
            out.writeInt(gcls.size());
            for (IRGlobalDef cls : gcls) {
                out.writeObject(cls);
            }
        }
        // 8) file2related
        out.writeInt(file2related.size());
        for (Map.Entry<String, HashSet<String>> e : file2related.entrySet()) {
            out.writeUTF(e.getKey());
            HashSet<String> related = e.getValue();
            out.writeInt(related.size());
            for (String name : related) {
                out.writeUTF(name);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        // 1) funcs
        int fsz = in.readInt();
        this.funcs = new HashMap<>(fsz);
        for (int i = 0; i < fsz; i++) {
            String name = in.readUTF();
            FuncInfo finfo = (FuncInfo) in.readObject();
            funcs.put(name, finfo);
        }
        // 2) classes
        int csz = in.readInt();
        this.classes = new HashMap<>(csz);
        for (int i = 0; i < csz; i++) {
            String name = in.readUTF();
            ClassInfo cinfo = (ClassInfo) in.readObject();
            classes.put(name, cinfo);
        }
        // 3) vars
        int vsz = in.readInt();
        this.vars = new HashMap<>(vsz);
        for (int i = 0; i < vsz; i++) {
            String name = in.readUTF();
            VarInfo vinfo = (VarInfo) in.readObject();
            vars.put(name, vinfo);
        }
        // 4) name2Size
        int msz = in.readInt();
        this.name2Size = new HashMap<>(msz);
        for (int i = 0; i < msz; i++) {
            String name = in.readUTF();
            int size = in.readInt();
            name2Size.put(name, size);
        }
        // 5) Gvars
        int gsz = in.readInt();
        this.Gvars = new HashMap<>(gsz);
        for (int i = 0; i < gsz; i++) {
            // 读回文件路径
            String path = in.readUTF();
            // 读回该文件下全局变量定义的数量
            int setSize = in.readInt();
            ArrayList<ASTVarDef> defs = new ArrayList<>(setSize);
            for (int j = 0; j < setSize; j++) {
                ASTVarDef def = (ASTVarDef) in.readObject();
                defs.add(def);
            }
            Gvars.put(path, defs);
        }
        // 6) file2name
        int f2nsz = in.readInt();
        this.file2name = new HashMap<>(f2nsz);
        for (int i = 0; i < f2nsz; i++) {
            String path = in.readUTF();
            int nsz = in.readInt();
            HashSet<String> names = new HashSet<>(nsz);
            for (int j = 0; j < nsz; j++) {
                names.add(in.readUTF());
            }
            file2name.put(path, names);
        }
        // 7) file2gcls
        int f2gcsz = in.readInt();
        this.file2gcls = new HashMap<>(f2gcsz);
        for (int i = 0; i < f2gcsz; i++) {
            String path = in.readUTF();
            int gcsz = in.readInt();
            HashSet<IRGlobalDef> gcls = new HashSet<>(gcsz);
            for (int j = 0; j < gcsz; j++) {
                IRGlobalDef cls = (IRGlobalDef) in.readObject();
                gcls.add(cls);
            }
            file2gcls.put(path, gcls);
        }
        // 8) file2related
        int f2rsz = in.readInt();
        this.file2related = new HashMap<>(f2rsz);
        for (int i = 0; i < f2rsz; i++) {
            String path = in.readUTF();
            int rsz = in.readInt();
            HashSet<String> related = new HashSet<>(rsz);
            for (int j = 0; j < rsz; j++) {
                related.add(in.readUTF());
            }
            file2related.put(path, related);
        }
        // basefunc/baseclass 已经在无参构造器里跑过一次了，无需再读
    }

    public WError collect(BaseScope scope, String filePath) {
        if (scope instanceof GlobalScope) {
            GlobalScope gscope = (GlobalScope) scope;
            for (var func : gscope.getFuncs().keySet()) {
                if (basefunc.contains(func)) {
                    continue;
                }
                if (!funcs.containsKey(func)) {
                    funcs.put(func, gscope.getFuncs().get(func));
                } else {
                    throw new WError("func Multi-define: " + func + " in " + filePath);
                }
            }
            for (var cls : gscope.getClasses().keySet()) {
                if (baseclass.contains(cls)) {
                    continue;
                }
                if (!classes.containsKey(cls)) {
                    classes.put(cls, gscope.getClasses().get(cls));
                } else {
                    throw new WError("class Multi-define: " + cls + " in " + filePath);
                }
            }
        } else {
            throw new WError("WorldScope.collect(BaseScope) should not be called");
        }
        return new WError();
    }

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
}
