package Compiler.Src.AST.Node.ExprNode.ExprUnitNode;

import java.util.ArrayList;

import Compiler.Src.AST.ASTVisitor;
import Compiler.Src.AST.Node.ExprNode.ASTExpr;
import Compiler.Src.IR.Entity.*;
import Compiler.Src.Util.Error.*;

@lombok.experimental.SuperBuilder
@lombok.Getter
@lombok.Setter
public class ASTConstarray extends ASTExpr {
    private int maze, dep;
    private ArrayList<ASTExpr> expr;
    private IRVariable dest;

    @Override
    public <T> T accept(ASTVisitor<T> visitor) throws BaseError {
        return visitor.visit(this);
    }

    // public ArrayList<IREntity> collectArgs(ArrayList<IRStrDef> strDefs) {
    //     var args = new ArrayList<IREntity>();
    //     for (var unit : expr) {
    //         if (!(unit instanceof ASTAtomExpr)) {
    //             throw new IRError("Constarray must be initialized with constant values");
    //         }
    //         if (((ASTAtomExpr) unit).getConstarray() == null) {
    //             if (((ASTAtomExpr) unit).getAtomType() == ASTAtomExpr.Type.INT) {
    //                 args.add(new IRLiteral(GlobalScope.irIntType, ((ASTAtomExpr) unit).getValue()));
    //             } else if (((ASTAtomExpr) unit).getAtomType() == ASTAtomExpr.Type.BOOL) {
    //                 args.add(new IRLiteral(GlobalScope.irBoolType, ((ASTAtomExpr) unit).getValue()));
    //             } else if (((ASTAtomExpr) unit).getAtomType() == ASTAtomExpr.Type.STRING) {
    //                 var dest = new IRVariable(GlobalScope.irPtrType, "@str." + (++IRCounter.strCount));
    //                 var str = new IRStrDef(dest, ((ASTAtomExpr) unit).getValue());
    //                 strDefs.add(str);
    //                 args.add(dest);
    //             } else {
    //                 throw new IRError("Constarray must be initialized with constant values");
    //             }
    //         }
    //         else{
    //             args.add(new IREntity(GlobalScope.irVoidType,"{"));
    //             args.addAll(((ASTAtomExpr) unit).getConstarray().collectArgs(strDefs));
    //             args.add(new IREntity(GlobalScope.irVoidType,"}"));
    //         }
    //     }
    //     return args;
    // }
}
