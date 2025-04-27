package Compiler.Src.IR.Type;

import java.io.Externalizable;

import Compiler.Src.Util.Info.TypeInfo;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRType implements Externalizable {
  public String typeName;

  public IRType() {
    this.typeName = null;
  }

  @Override
  public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
    out.writeUTF(typeName);
  }
  @Override
  public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
    this.typeName = in.readUTF();
  }

  public IRType(String typename) {
    this.typeName = typename;
  }

  public IRType(TypeInfo type) {
    if (type.equals(GlobalScope.intType)) {
      this.typeName = "i32";
    } else if (type.equals(GlobalScope.boolType)) {
      this.typeName = "i1";
    } else if (type.equals(GlobalScope.voidType)) {
      this.typeName = "void";
    } else {
      this.typeName = "ptr";
    }
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs instanceof IRType) {
      return this.typeName.equals(((IRType) rhs).getTypeName());
    }
    return false;
  }

  @Override
  public String toString() {
    return typeName;
  }

  @Override
  public int hashCode() {
    return typeName.hashCode();
  }
}