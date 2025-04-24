package Compiler.Src.AST.Node;


import Compiler.Src.Util.position;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import Compiler.Src.AST.*;
import Compiler.Src.Util.Error.*;
import lombok.*;
@lombok.experimental.SuperBuilder
@Getter
@Setter
public class ASTNode implements Externalizable {
    protected transient ASTNode parent;
    protected position pos;
    protected ASTNode() {
        this.parent = null;
        this.pos = new position(0, 0);  // assuming position constructor requires row and column
    }

    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    public String str() {
        return pos.str();
    }
     /**
     * 写出本类字段：跳过 parent，写出 pos.row/pos.col
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // 注意顺序：readExternal 必须按相同顺序读回
        out.writeInt(pos.getRow());
        out.writeInt(pos.getColumn());
    }

    /**
     * 读回本类字段：按 writeExternal 中的顺序读取
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        int row = in.readInt();
        int col = in.readInt();
        this.pos = new position(row, col);
        // parent 保持 null，子类或外部调用可以在反序列化后
        // 重新遍历树并 setParent(...) 来恢复父子关系
        this.parent = null;
    }
}