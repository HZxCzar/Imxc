package Compiler.Src.Util.Error;

@lombok.Getter
@lombok.Setter
public class ASTError extends BaseError{

    public ASTError() {
        super("");
    }

    public ASTError(String msg) {
        super(msg);
    }

    public void append(String msg) {
        message += msg;
    }

    public void append(ASTError msg) {
        message += msg.getMessage();
    }
}
