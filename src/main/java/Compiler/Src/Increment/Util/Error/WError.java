package Compiler.Src.Increment.Util.Error;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class WError extends BaseError{

    public WError() {
        super("");
    }

    public WError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(WError msg) {
        message += msg.getMessage();
    }
}
