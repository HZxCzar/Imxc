package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class OPTError extends BaseError{

    public OPTError() {
        super("");
    }

    public OPTError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(OPTError msg) {
        message += msg.getMessage();
    }
}