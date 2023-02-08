package org.mel.parser.ast;

import org.mel.parser.JRuntime;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.TokenException;

import java.util.List;
import java.util.Map;

public class MultipleBinaryAst implements Ast {

    final Ast left;
    final List<String> ops;
    final List<Ast> right;

    public MultipleBinaryAst(Ast left, List<String> ops, List<Ast> right) {
        super();
        this.left = left;
        this.ops = ops;
        this.right = right;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        Object r = left.evaluate(model);
        for (int i = 0; i < ops.size(); i++) {
            String op = ops.get(i);
            try {
                Object rightVal = right.get(i).evaluate(model);

                if ("+".equals(op)) {
                    r = JRuntime.add(r, rightVal);
                } else if ("-".equals(op)) {
                    r = JRuntime.sub(r, rightVal);
                } else if ("*".equals(op)) {
                    r = JRuntime.mul(r, rightVal);
                } else if ("/".equals(op)) {
                    r = JRuntime.div(r, rightVal);
                } else if ("%".equals(op)) {
                    r = JRuntime.mod(r, rightVal);
                } else if ("and".equals(op) || "&&".equals(op)) {
                    r = JRuntime.and(r, rightVal);
                } else if ("or".equals(op) || "||".equals(op)) {
                    r = JRuntime.or(r, rightVal);
                } else {
                    throw new RuntimeException(op);
                }

            } catch (Exception e) {
                throw new TokenException(right.get(i).getSourceRef(), "error operating: " + op, e);
            }
        }
        return r;
    }

    @Override
    public String toString() {
        if (ops.isEmpty()) {
            return String.valueOf(left);
        } else {
            StringBuilder s = new StringBuilder();
            s.append("(");
            s.append(left);
            for (int i = 0; i < ops.size(); i++) {
                s.append(" " + ops.get(i) + " ");
                s.append(right.get(i));
            }
            s.append(")");
            return s.toString();
        }
    }

    @Override
    public SourceRef getSourceRef() {
        return left.getSourceRef();
    }

}