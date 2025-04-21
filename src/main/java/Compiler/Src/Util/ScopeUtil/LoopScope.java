package Compiler.Src.Util.ScopeUtil;

import Compiler.Src.Util.Info.*;

// @lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class LoopScope extends BaseScope {
    private int loopCnt;

    public LoopScope(BaseScope parent, BaseInfo info) {
        super(parent, info);
        this.loopCnt = 0;
    }
}
