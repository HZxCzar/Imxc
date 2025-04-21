package Compiler.Src.IR.Node.Stmt;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Node.IRNode;
import Compiler.Src.IR.Node.Inst.IRInst;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.IRError;

@lombok.Getter
@lombok.Setter
public class IRStmt extends IRNode {
    private ArrayList<IRInst> insts;
    private IREntity dest;
    private IREntity destAddr;

    public IRStmt() {
        insts = new ArrayList<IRInst>();
    }

    public void addInsts(IRInst inst) {
        insts.add(inst);
    }

    public void addBlockInsts(IRStmt insts)
    {
        this.insts.addAll(insts.getInsts());
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public IRLabel getLastLabel() {
        for (var i = insts.size() - 1; i >= 0; --i) {
          if (insts.get(i) instanceof IRLabel) {
            return (IRLabel) insts.get(i);
          }
        }
        return null;
      }

    public void addFront(IRInst node) {
        insts.add(0, node);
    }

    public void addFrontBlockInsts(IRStmt insts)
    {
        this.insts.addAll(0,insts.getInsts());
    }

    @Override
    public String toString() {
        throw new IRError("IRStmt.toString() is not implemented");
    }
}
