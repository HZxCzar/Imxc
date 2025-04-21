package Compiler.Src.Util.Info;
@lombok.Getter
@lombok.Setter
public class BaseInfo {
    private final String name;

    public BaseInfo(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return false;
    }
}
