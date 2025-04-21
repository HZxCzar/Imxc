package Compiler.Src.IR.Node.util;

import java.util.Objects;

import Compiler.Src.IR.Node.Inst.IRInst;

@lombok.Getter
@lombok.Setter
public class IRLabel extends IRInst{
    private String label;
    int loopDepth;
    // public IRLabel(String label) {
    //     super(-1);
    //     this.label = label;
    //     this.loopDepth = 0;
    // }
    public IRLabel(String label,int loopDepth) {
        super(-1);
        this.label = label;
        this.loopDepth = loopDepth;
    }
    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IRLabel) {
            return label.equals(((IRLabel) obj).getLabel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
