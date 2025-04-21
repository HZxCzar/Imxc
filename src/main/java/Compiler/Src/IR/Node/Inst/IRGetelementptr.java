package Compiler.Src.IR.Node.Inst;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.misc.Pair;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IREntity;
import Compiler.Src.IR.Entity.IRLiteral;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.Util.Error.ASMError;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRGetelementptr extends IRInst {
  private String type;
  private IRVariable dest;
  private IREntity ptr;
  private ArrayList<IREntity> infolist;

  // private ArrayList<IRVariable> indexlist;
  public IRGetelementptr(int id, IRVariable dest, String type, IREntity ptr, ArrayList<IREntity> info) {
    super(id);
    this.type = type;
    this.dest = dest;
    this.ptr = ptr;
    this.infolist = info;
  }

  public IREntity Innercompute(HashMap<IRVariable, Pair<Integer, IREntity>> varMap) {
    var second = infolist.get(infolist.size() - 1);
    if (ptr instanceof IRVariable && varMap.get((IRVariable) ptr).a == 0) {
      return null;
    } else if (second instanceof IRVariable && varMap.get((IRVariable) second).a == 0) {
      return null;
    }
    int lval = ptr instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) ptr).b.getValue().equals("null")?"0":varMap.get((IRVariable) ptr).b.getValue())
        : Integer.parseInt(ptr.getValue().equals("null")?"0":ptr.getValue());
    int rval = second instanceof IRVariable ? Integer.parseInt(varMap.get((IRVariable) second).b.getValue())
        : Integer.parseInt(second.getValue());
    var res = new IRLiteral(dest.getType(), "0");
    res.setValue(Integer.toString(lval + 4 * rval));
    return res;
  }

  @Override
  public <T> T accept(IRVisitor<T> visitor) throws BaseError {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    String str = dest.getValue() + " = getelementptr " + type.toString() + ", " + ptr.toString() + ", ";
    for (int i = 0; i < infolist.size(); i++) {
      str += infolist.get(i).getType().toString() + " " + infolist.get(i).getValue();
      if (i != infolist.size() - 1) {
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
    if (ptr instanceof IRVariable) {
      res.add((IRVariable) ptr);
    }
    for (var info : infolist) {
      if (info instanceof IRVariable) {
        res.add((IRVariable) info);
      }
    }
    return res;
  }

  @Override
  public void replaceUse(IRVariable oldVar, IREntity newVar) {
    if (ptr.equals(oldVar)) {
      ptr = newVar;
    }
    for (int i = 0; i < infolist.size(); i++) {
      if (infolist.get(i).equals(oldVar)) {
        infolist.set(i, newVar);
      }
    }
  }
}
