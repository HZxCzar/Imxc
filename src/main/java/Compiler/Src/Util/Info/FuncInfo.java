package Compiler.Src.Util.Info;

import java.util.ArrayList;

import Compiler.Src.AST.Node.DefNode.*;
@lombok.Getter
@lombok.Setter
public class FuncInfo extends BaseInfo {
    TypeInfo functype;
    ArrayList<TypeInfo> params;
    public FuncInfo(String name,TypeInfo type,TypeInfo... params)
    {
        super(name);
        this.functype=type;
        this.params = new ArrayList<TypeInfo>();
        for(TypeInfo parm:params)
        {
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
