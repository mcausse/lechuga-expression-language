package org.mel.parser.ast;

import org.mel.parser.JRuntime;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.TokenException;

import java.util.List;
import java.util.Map;

public class Exp6Ast implements Ast {

    final Ast left;
    final List<Ast> right;

    public Exp6Ast(Ast left, List<Ast> right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        Object r = left.evaluate(model);
        for (Ast ri : right) {
            try {
                Object key = ri.evaluate(model);
                r = JRuntime.getByKey(r, key);
            } catch (Exception e) {
                throw new TokenException(ri.getSourceRef(), "error operating: ", e);
            }
        }
        return r;
    }

    @Override
    public String toString() {
        if (right.isEmpty()) {
            return String.valueOf(left);
        } else {
            StringBuilder s = new StringBuilder();
            s.append("(");
            s.append(left);
            for (Ast i : right) {
                s.append("[");
                s.append(i);
                s.append("]");
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