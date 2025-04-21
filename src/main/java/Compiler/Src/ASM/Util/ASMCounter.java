package Compiler.Src.ASM.Util;

import java.util.TreeMap;

import Compiler.Src.ASM.Entity.ASMVirtualReg;

public class ASMCounter {
    public static int funcCount = -1;
    public static int InstCount = 0;
    public static int allocaCount;
    public TreeMap<String, ASMVirtualReg> name2reg;

    public ASMCounter() {
        funcCount++;
        // this.allocaCount = 2;
        this.name2reg = new TreeMap<String, ASMVirtualReg>();
    }
}
