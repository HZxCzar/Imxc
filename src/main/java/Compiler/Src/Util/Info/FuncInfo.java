package Compiler.Src.Util.Info;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import Compiler.Src.AST.Node.DefNode.*;

@lombok.Getter
@lombok.Setter
public class FuncInfo extends BaseInfo{
  TypeInfo functype;
  ArrayList<TypeInfo> params;

  /**
   * 必须要有一个 public 的无参构造器，
   * Externalizable 在反序列化时会先调用它，然后再调用 readExternal
   */
  public FuncInfo() {
    // 如果 BaseInfo 没有无参构造，必须调用 super(…)：
    super("");
    this.params = new ArrayList<>();
  }

  /**
   * 序列化：把需要保存的字段依次写入
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    // 1. 写 BaseInfo 的 name
    super.writeExternal(out);
    // 2. 写函数返回类型
    out.writeObject(functype);
    // 3. 写参数列表长度和每个参数类型
    out.writeInt(params.size());
    for (TypeInfo t : params) {
      out.writeObject(t);
    }
  }

  /**
   * 反序列化：按 writeExternal 的顺序读回字段
   */
  @Override
  public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException {
    // 1. 读 name 并赋值给 BaseInfo
    super.readExternal(in);

    // 2. 读返回类型
    this.functype = (TypeInfo) in.readObject();

    // 3. 读参数列表
    int size = in.readInt();
    this.params = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      this.params.add((TypeInfo) in.readObject());
    }
  }

  public FuncInfo(String name, TypeInfo type, TypeInfo... params) {
    super(name);
    this.functype = type;
    this.params = new ArrayList<TypeInfo>();
    for (TypeInfo parm : params) {
      this.params.add(parm);
    }
  }

  public FuncInfo(String name, TypeInfo type, ArrayList<ASTVarDef> params) {
    super(name);
    this.functype = type;
    this.params = new ArrayList<TypeInfo>();
    for (ASTVarDef param : params) {
      this.params.add(((VarInfo) param.getInfo()).getType());
    }
  }
}
