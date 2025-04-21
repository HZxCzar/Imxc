package Compiler.Src.ASM.Entity;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
public class ASMPhysicalReg extends ASMReg {
    int color;

    public ASMPhysicalReg(String name) {
        super(name);
        switch (name) {
            case "zero":
                color = 0;
                break;
            case "ra":
                color = 1;
                break;
            case "sp":
                color = 2;
                break;
            case "gp":
                color = 3;
                break;
            case "tp":
                color = 4;
                break;
            case "t0":
                color = 5;
                break;
            case "t1":
                color = 6;
                break;
            case "t2":
                color = 7;
                break;
            case "s0":
                color = 8;
                break;
            case "s1":
                color = 9;
                break;
            case "a0":
                color = 10;
                break;
            case "a1":
                color = 11;
                break;
            case "a2":
                color = 12;
                break;
            case "a3":
                color = 13;
                break;
            case "a4":
                color = 14;
                break;
            case "a5":
                color = 15;
                break;
            case "a6":
                color = 16;
                break;
            case "a7":
                color = 17;
                break;
            case "s2":
                color = 18;
                break;
            case "s3":
                color = 19;
                break;
            case "s4":
                color = 20;
                break;
            case "s5":
                color = 21;
                break;
            case "s6":
                color = 22;
                break;
            case "s7":
                color = 23;
                break;
            case "s8":
                color = 24;
                break;
            case "s9":
                color = 25;
                break;
            case "s10":
                color = 26;
                break;
            case "s11":
                color = 27;
                break;
            case "t3":
                color = 28;
                break;
            case "t4":
                color = 29;
                break;
            case "t5":
                color = 30;
                break;
            case "t6":
                color = 31;
                break;
            default:
                throw new RuntimeException("Unknown physical register: " + name);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASMPhysicalReg) {
            return name.equals(((ASMPhysicalReg) obj).name);
        }
        return false;
    }

    @Override
    public int compareTo(ASMReg reg) {
        return name.compareTo(reg.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, -1);
    }
}
