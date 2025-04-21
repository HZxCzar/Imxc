package Compiler.Src.Util.Info;
@lombok.Getter
@lombok.Setter
public class VarInfo extends BaseInfo {
    TypeInfo type;
    public VarInfo(String name, TypeInfo type) {
        super(name);
        this.type = type;
    }
}
