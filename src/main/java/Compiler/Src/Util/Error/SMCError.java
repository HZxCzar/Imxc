package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class SMCError extends BaseError{

    public SMCError() {
        super("");
    }

    public SMCError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(SMCError msg) {
        message += msg.getMessage();
    }
}
