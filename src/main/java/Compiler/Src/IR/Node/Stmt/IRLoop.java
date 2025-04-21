package Compiler.Src.IR.Node.Stmt;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Node.Inst.*;
import Compiler.Src.IR.Node.util.IRLabel;
import Compiler.Src.IR.Util.IRControl;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.BaseError;

@lombok.Getter
@lombok.Setter
public class IRLoop extends IRStmt {
    public static int count = 0;

    public IRLoop(int num, IRStmt init, IRStmt cond, IRStmt update, IRStmt body, int loopDepth) {
        var condLabel = new IRLabel("loop." + String.valueOf(num) + ".condLabel", loopDepth);
        var updateLabel = new IRLabel("loop." + String.valueOf(num) + ".updateLabel", loopDepth);
        var bodyLabel = new IRLabel("loop." + String.valueOf(num) + ".bodyLabel", loopDepth);
        var endLabel = new IRLabel("loop." + String.valueOf(num) + ".endLabel", loopDepth-1);
        if (init != null) {
            addBlockInsts(init);
        }
        addInsts(new IRBranch(++InstCounter.InstCounter, condLabel));

        // addInsts(condLabel);
        // if (cond != null) {
        //     addBlockInsts(cond);
        //     addInsts(new IRBranch(++InstCounter.InstCounter, cond.getDest(), bodyLabel, endLabel));
        // } else {
        //     addInsts(new IRBranch(++InstCounter.InstCounter, bodyLabel));
        // }

        addInsts(bodyLabel);
        addBlockInsts(body);
        addInsts(new IRBranch(++InstCounter.InstCounter, updateLabel));

        addInsts(updateLabel);
        if (update != null) {
            addBlockInsts(update);
        }
        addInsts(new IRBranch(++InstCounter.InstCounter, condLabel));

        addInsts(condLabel);
        if (cond != null) {
            addBlockInsts(cond);
            addInsts(new IRBranch(++InstCounter.InstCounter, cond.getDest(), bodyLabel, endLabel));
        } else {
            addInsts(new IRBranch(++InstCounter.InstCounter, bodyLabel));
        }
        
        addInsts(endLabel);
    }

    public static int addCount() {
        return ++count;
    }

    public static void initCount() {
        count = 0;
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        throw new Error("IRLoop.toString() is not implemented");
    }
}
