package org.mel.parser.ast;

import org.mel.parser.JRuntime;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.TokenException;

import java.util.Map;

public class SingleBinaryAst implements Ast {

    final Ast left;
    final String op;
    final Ast right;

    public SingleBinaryAst(Ast left, String op, Ast right) {
        super();
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        Object r = left.evaluate(model);
        if (op != null) {
            try {
                Object rightVal = right.evaluate(model);
                if ("eq".equals(op) || "==".equals(op)) {
                    r = JRuntime.eq(r, rightVal);
                } else if ("ne".equals(op) || "==".equals(op)) {
                    r = JRuntime.ne(r, rightVal);
                } else if ("lt".equals(op) || "<".equals(op)) {
                    r = JRuntime.lt(r, rightVal);
                } else if ("gt".equals(op) || ">".equals(op)) {
                    r = JRuntime.gt(r, rightVal);
                } else if ("le".equals(op) || "<=".equals(op)) {
                    r = JRuntime.le(r, rightVal);
                } else if ("ge".equals(op) || ">=".equals(op)) {
                    r = JRuntime.ge(r, rightVal);
                } else {
                    throw new RuntimeException(op);
                }
            } catch (Exception e) {
                throw new TokenException(left.getSourceRef(), "error operating: " + op, e);
            }
        }
        return r;
    }

    @Override
    public String toString() {
        if (op == null) {
            return String.valueOf(left);
        } else {
            return "(" + left + " " + op + " " + right + ")";
        }
    }

    @Override
    public SourceRef getSourceRef() {
        return left.getSourceRef();
    }

}
