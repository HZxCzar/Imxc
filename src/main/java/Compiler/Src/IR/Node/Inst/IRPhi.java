package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;

import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRPhi extends IRInst {
  private IRVariable dest;
  private IRType type;
  private ArrayList<IREntity> vals;
  private ArrayList<IRLabel> labels;

  public IRPhi(int id, IRVariable dest, IRType type, ArrayList<IREntity> vals, ArrayList<IRLabel> labels) {
    super(id);
    this.dest = dest;
    this.type = type;
    this.vals = vals;
    this.labels = labels;
  }

  @Override
  public <T> T accept(IRVisitor<T> visitor) throws BaseError {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    var str = dest.getValue() + " = phi " + type.toString() + " ";
    for (int i = 0; i < vals.size(); i++) {
      str += "[ " + vals.get(i).getValue() + ", %" + labels.get(i).toString() + " ]";
      if (i != vals.size() - 1) {
        str += ", ";
      }
    }
    return str;
  }

  @Override
  public IRVariable getDest() {
    return dest;
  }

  @Override
  public IRVariable getDef() {
    return dest;
  }

  @Override
  public ArrayList<IRVariable> getUses() {
    ArrayList<IRVariable> res = new ArrayList<>();
    for (var val : vals) {
      if (val instanceof IRVariable) {
        res.add((IRVariable) val);
      }
    }
    return res;
  }

  @Override
  public void replaceUse(IRVariable oldVar, IREntity newVar) {
    for (int i = 0; i < vals.size(); i++) {
      if (vals.get(i).equals(oldVar)) {
        vals.set(i, newVar);
      }
    }
  }
}
