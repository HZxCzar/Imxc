package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class ScopeError extends BaseError{

    public ScopeError() {
        super("");
    }

    public ScopeError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(ScopeError msg) {
        message += msg.getMessage();
    }
}
