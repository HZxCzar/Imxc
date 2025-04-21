package Compiler.Src.Util.Info;

// import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class TypeInfo extends BaseInfo {
    private int depth;
    private boolean defined;

    public TypeInfo(String name, int depth) {
        super(name);
        this.depth = depth;
        if (name.equals("int") || name.equals("void") || name.equals("bool") || name.equals("string")
                || name.equals("null")) {
            this.defined = true;
        } else {
            this.defined = false;
        }
    }

    @Override
    public boolean equals(Object rhs) {
        if (!(rhs instanceof TypeInfo)) {
            return false;
        }
        if (this.getName().equals(((TypeInfo) rhs).getName()) && this.depth == ((TypeInfo) rhs).depth) {
            return true;
        }
        return false;
    }
}
