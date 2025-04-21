package Compiler.Src.IR.Node.Def;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRStrDef extends IRGlobalDef {
    public String value_old;
    public String value;
    public int length = 0;

    public IRStrDef(IRVariable dest, String value_old) {
        super(dest);
        this.value_old = value_old;
        String ret = "";
        for (int i = 0; i < value_old.length(); ++i) {
            char c = value_old.charAt(i);
            switch (c) {
                case '\n':
                    ret += "\\0A";
                    break;
                case '\"':
                    ret += "\\22";
                    break;
                case '\\':
                    ret += "\\\\";
                    break;
                default:
                    ret += c;
            }
            length++;
        }
        this.value = ret + "\\00";
        length++;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return getVars().getValue() + " = constant" + String.format(" [%d x i8] c", length) + "\"" + value + "\"";
    }
}
