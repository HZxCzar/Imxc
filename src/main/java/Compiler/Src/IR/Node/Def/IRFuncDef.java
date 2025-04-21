package Compiler.Src.IR.Node.Def;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import Compiler.Src.IR.IRVisitor;
import Compiler.Src.IR.Entity.IRVariable;
import Compiler.Src.IR.Node.Inst.IRAlloca;
import Compiler.Src.IR.Node.Stmt.IRBlock;
import Compiler.Src.IR.Node.Stmt.IRStmt;
import Compiler.Src.IR.Node.Inst.IRStore;
import Compiler.Src.IR.Type.IRType;
import Compiler.Src.IR.Util.InstCounter;
import Compiler.Src.Util.Error.BaseError;
import Compiler.Src.Util.ScopeUtil.GlobalScope;

@lombok.Getter
@lombok.Setter
public class IRFuncDef extends IRDef {
    private String name;
    private ArrayList<IRVariable> params;
    private IRType returnType;
    private ArrayList<IRBlock> blockstmts;
    private boolean isBuiltIn = false;

    private IRBlock endBlock;

    //CFG
    // private TreeMap<IRBlock, Integer> block2Order = new TreeMap<IRBlock, Integer>();
    private ArrayList<IRBlock> order2Block;

    public IRFuncDef(String name, ArrayList<IRVariable> params, IRType returnType, ArrayList<IRBlock> blockstmts) {
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.blockstmts = blockstmts;
        this.isBuiltIn = false;
        if (blockstmts.size() != 0) {
            var entryBlock = blockstmts.get(0);
            if (params.size() > 0) {
                var instList = new IRStmt();
                for (var param : params) {
                    var paramPtr = new IRVariable(GlobalScope.irPtrType, param.getValue().replace(".param", ""));
                    instList.addInsts(new IRAlloca(++InstCounter.InstCounter,paramPtr, param.getType()));
                    instList.addInsts(new IRStore(++InstCounter.InstCounter,paramPtr, param));
                }
                entryBlock.addFrontBlockInsts(instList);
            }
        }

        //CFG
        // this.block2Order = new TreeMap<IRBlock, Integer>();
        this.order2Block = new ArrayList<IRBlock>();
    }

    public IRFuncDef(String name, IRType returnType, ArrayList<IRType> params) {
        this.name = name;
        this.returnType = returnType;
        this.isBuiltIn = true;
        this.params = new ArrayList<IRVariable>();
        for (int i = 0; i < params.size(); i++) {
            this.params.add(new IRVariable(params.get(i), "null"));
        }
    }

    @Override
    public <T> T accept(IRVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        if (isBuiltIn) {
            String str = "declare " + returnType.toString() + " @" + name + "(";
            for (int i = 0; i < params.size(); i++) {
                str += params.get(i).getType().toString();
                if (i != params.size() - 1)
                    str += ", ";
            }
            str += ")\n";
            return str;
        } else {
            String str = "define " + returnType.toString() + " @" + name + "(";
            for (int i = 0; i < params.size(); i++) {
                str += params.get(i).toString();
                if (i != params.size() - 1)
                    str += ", ";
            }
            str += ") {\n";
            for (IRBlock blockstmt : blockstmts) {
                str += blockstmt.toString();
            }
            str += "}\n";
            return str;
        }
    }
}
