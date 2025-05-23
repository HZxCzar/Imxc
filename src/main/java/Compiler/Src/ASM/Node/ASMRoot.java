package Compiler.Src.ASM.Node;

import java.util.ArrayList;

import Compiler.Src.ASM.ASMVisitor;
import Compiler.Src.ASM.Node.Global.*;

@lombok.Getter
@lombok.Setter
public class ASMRoot extends ASMNode {
    private ArrayList<ASMVarDef> vars;
    private ArrayList<ASMStrDef> strs;
    private ArrayList<ASMFuncDef> funcs;

    public ASMRoot() {
        vars = new ArrayList<ASMVarDef>();
        strs = new ArrayList<ASMStrDef>();
        funcs = new ArrayList<ASMFuncDef>();
    }

    public void Dinit() {
        for (ASMFuncDef func : funcs) {
            if(func.getName().equals("main.global.init")){
                funcs.remove(func);
                break;
            }
        }
    }

    public void combine(ASMRoot other, Boolean first) {
        for (ASMVarDef var : other.vars) {
            this.vars.add(var);
        }
        for (ASMStrDef str : other.strs) {
            this.strs.add(str);
        }
        for (ASMFuncDef func : other.funcs) {
            if(!first && func.getName().equals("main.global.init")) continue;
            this.funcs.add(func);
        }
    }

    @Override
    public String toString() {
        String str = "";
        if (vars.size() > 0) {
            str += ".data\n";
            for (var var : vars) {
                str += var.toString() + "\n";
            }
            str += "\n";
        }

        if (strs.size() > 0) {
            str += ".rodata\n";
            for (var strUnit : strs) {
                str += strUnit.toString() + "\n";
            }
            str += "\n";
        }

        if (funcs.size() > 0) {
            for (var func : funcs) {
                str += ".text\n";
                str += func.toString() + "\n";
            }
        }
        return str;
    }

    @Override
    public <T> T accept(ASMVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
