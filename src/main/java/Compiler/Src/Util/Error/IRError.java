package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class IRError extends BaseError{

    public IRError() {
        super("");
    }

    public IRError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(IRError msg) {
        message += msg.getMessage();
    }
}
