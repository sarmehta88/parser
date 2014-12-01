package ast;

import visitor.*;

public class RepeatTree extends AST {

    public RepeatTree() {
    }// default constructor 

    public Object accept(ASTVisitor v) {
        return v.visitRepeatTree(this);
    }

}