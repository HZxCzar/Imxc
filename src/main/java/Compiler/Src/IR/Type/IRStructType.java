package Compiler.Src.IR.Type;

import java.util.ArrayList;


@lombok.Getter
@lombok.Setter
public class IRStructType extends IRType {
  public ArrayList<IRType> members;
  public Boolean aline;
  public int size;

  public IRStructType(String typeName, ArrayList<IRType> members, Boolean aline) {
    super(typeName);
    this.members = members;
    this.aline = aline;
  }

  @Override
  public String toString() {
    String str = "";
    if (!aline) {
      str += "<";
    }
    str += "{ ";
    for (int i = 0; i < members.size(); i++) {
      str += members.get(i).toString();
      if (i != members.size() - 1)
        str += ", ";
    }
    str += " }";
    if (!aline) {
      str += ">";
    }
    return str;
  }
}
