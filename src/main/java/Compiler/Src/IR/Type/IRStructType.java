package Compiler.Src.IR.Type;

import java.util.ArrayList;


@lombok.Getter
@lombok.Setter
public class IRStructType extends IRType{
  public ArrayList<IRType> members;
  public Boolean aline;
  public int size;

  public IRStructType() {
    super();
    this.members = null;
    this.aline = true;
  }

  @Override
  public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
    // 先让父类写自己的内容
    super.writeExternal(out);
    // 写 aline
    out.writeBoolean(aline);
    // 写 size
    out.writeInt(size);
    // 写 members 的数量
    out.writeInt(members != null ? members.size() : 0);
    // System.out.println("numMembers: " + members.size() + typeName);
    // 写每个 member
    if (members != null) {
        for (IRType member : members) {
            out.writeObject(member);
        }
    }
  }
  @Override
  public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
    // 先让父类读自己的内容
    super.readExternal(in);
    // 读 aline
    this.aline = in.readBoolean();
    // 读 size
    this.size = in.readInt();
    // 读 members 的数量
    int numMembers = in.readInt();
    // System.out.println("numMembers: " + numMembers + typeName);
    this.members = new ArrayList<>(numMembers);
    for (int i = 0; i < numMembers; ++i) {
        IRType member = (IRType) in.readObject();
        this.members.add(member);
    }
  }

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
