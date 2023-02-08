package org.mel.parser.ast;

import org.mel.parser.JRuntime;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.TokenException;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class UnaryAst implements Ast {

    final List<String> ops;
    final Ast right;

    public UnaryAst(List<String> ops, Ast right) {
        super();
        this.ops = ops;
        this.right = right;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        Object r = right.evaluate(model);
        for (int i = ops.size() - 1; i >= 0; i--) {
            String op = ops.get(i);
            try {
                if ("not".equals(op)) {
                    r = JRuntime.not(r);
                } else if ("byte".equals(op)) {
                    r = JRuntime.toByte(r);
                } else if ("short".equals(op)) {
                    r = JRuntime.toShort(r);
                } else if ("int".equals(op)) {
                    r = JRuntime.toInteger(r);
                } else if ("long".equals(op)) {
                    r = JRuntime.toLong(r);
                } else if ("float".equals(op)) {
                    r = JRuntime.toFloat(r);
                } else if ("double".equals(op)) {
                    r = JRuntime.toDouble(r);
                } else if ("string".equals(op)) {
                    r = JRuntime.toString(r);
                } else if ("-".equals(op)) {
                    r = JRuntime.neg(r);
                } else {
                    throw new RuntimeException(op);
                }
            } catch (Exception e) {
                throw new TokenException(right.getSourceRef(), "error operating: " + op, e);
            }
        }
        return r;
    }

    @Override
    public String toString() {

        if (!ops.isEmpty()) {
            StringJoiner j = new StringJoiner(" ");
            for (String op : ops) {
                j.add(op);
            }
            return "(" + j + " " + right + ")";
        }
        return right.toString();
    }

    @Override
    public SourceRef getSourceRef() {
        return right.getSourceRef();
    }

}