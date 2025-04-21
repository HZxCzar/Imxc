package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class ASMError extends BaseError{

    public ASMError() {
        super("");
    }

    public ASMError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(ASMError msg) {
        message += msg.getMessage();
    }
}
