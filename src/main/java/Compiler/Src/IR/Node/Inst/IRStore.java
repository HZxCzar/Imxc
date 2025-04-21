package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.Util.Error.BaseError;
// import Compiler.Src.Util.ScopeUtil.GlobalScope;
import Compiler.Src.Util.Error.IRError;

@lombok.Getter
@lombok.Setter
public class IRStore extends IRInst {
  private IRVariable dest;
  private IREntity src;

  public IRStore(int id, IRVariable dest, IREntity src) {
    super(id);
    this.dest = dest;
    this.src = src;
  }

  @Override
  public <T> T accept(IRVisitor<T> visitor) throws BaseError {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return "store " + src.toString() + ", " + dest.toString();
  }

  @Override
  public IRVariable getDef() {
    return null;
  }

  @Override
  public ArrayList<IRVariable> getUses() {
    ArrayList<IRVariable> res = new ArrayList<>();
    if (src instanceof IRVariable) {
      res.add((IRVariable) src);
    }
    if (dest instanceof IRVariable) {
      res.add(dest);
    }
    return res;
  }

  @Override
  public void replaceUse(IRVariable oldVar, IREntity newVar) {
    if (src.equals(oldVar)) {
      src = newVar;
    }
    if (dest.equals(oldVar)) {
      dest = (IRVariable) newVar;
    }
  }
}
