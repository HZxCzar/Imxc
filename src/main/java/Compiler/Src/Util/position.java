package Compiler.Src.Util;

import org.antlr.v4.runtime.Token;
@lombok.Getter
@lombok.Setter
public class position {
    private int row, column;

    public position(int row, int col) {
        this.row = row;
        this.column = col;
    }

    public position(Token token) {
    this.row = token.getLine();
    this.column = token.getCharPositionInLine();
  }

    public String str() {
        return "row: " + row + " | column: " + column;
    }
}
