package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class SBCError extends BaseError{

    public SBCError() {
        super("");
    }

    public SBCError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(SBCError msg) {
        message += msg.getMessage();
    }
}
