package Compiler.Src.IR.Node.Stmt;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.Util.IRControl;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.Error.IRError;

@lombok.Getter
@lombok.Setter
public class IRIf extends IRStmt {
  public static int count = 0;
  private IRLabel condLabel, bodyLabel, elseLabel;

  public IRIf(int num, IRStmt cond, IRStmt body, IRStmt elseBody, int loopDepth) {
    var condLabel = new IRLabel( "if." + String.valueOf(num) + ".cond", loopDepth);
    var bodyLabel = new IRLabel("if." + String.valueOf(num) + ".body", loopDepth);
    var elseLabel = new IRLabel("if." + String.valueOf(num) + ".else", loopDepth);
    var endLabel = new IRLabel( "if." + String.valueOf(num) + ".end", loopDepth);
    addInsts(new IRBranch(++InstCounter.InstCounter, condLabel));
    cond.addFront(condLabel);
    addBlockInsts(cond);
    this.condLabel = cond.getLastLabel();
    addInsts(
        new IRBranch(++InstCounter.InstCounter, cond.getDest(), body == null ? endLabel : bodyLabel,
            elseBody == null ? endLabel : elseLabel));
    if (body != null) {
      body.addFront(bodyLabel);
      this.bodyLabel = body.getLastLabel();
      addBlockInsts(body);
      addInsts(new IRBranch(++InstCounter.InstCounter, endLabel));
    }
    if (elseBody != null) {
      elseBody.addFront(elseLabel);
      this.elseLabel = elseBody.getLastLabel();
      addBlockInsts(elseBody);
      addInsts(new IRBranch(++InstCounter.InstCounter, endLabel));
    }
    addInsts(endLabel);
  }

  public static int addCount() {
    return ++count;
  }

  @Override
  public <T> T accept(IRVisitor<T> visitor) throws BaseError {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    throw new IRError("IRIf.toString() is not implemented");
  }
}
