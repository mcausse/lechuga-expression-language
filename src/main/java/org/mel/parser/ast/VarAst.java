package org.mel.parser.ast;

import org.mel.parser.JRuntime;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.TokenException;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class VarAst implements Ast {

    final SourceRef sourceRef;
    final List<String> idents;

    public VarAst(SourceRef sourceRef, List<String> idents) {
        super();
        this.sourceRef = sourceRef;
        this.idents = idents;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        try {
            Object o = model;
            for (String i : idents) {
                if (o == null) {
                    throw new TokenException(sourceRef, "nullpointer evaluating: " + i + " in " + idents);
                }
                o = JRuntime.getByKey(o, i);
            }
            return o;
        } catch (Exception e) {
            throw new TokenException(sourceRef, "evaluating: " + idents, e);
        }
    }

    @Override
    public String toString() {
        StringJoiner s = new StringJoiner(".");
        for (String i : idents) {
            s.add(i);
        }
        return s.toString();
    }

    @Override
    public SourceRef getSourceRef() {
        return sourceRef;
    }

}