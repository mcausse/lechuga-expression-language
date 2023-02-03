package org.mel.parser.ast;

import org.mel.tokenizer.SourceRef;

import java.util.Map;

public class ExpNAst implements Ast {

    final SourceRef sourceRef;
    final Object value;

    public ExpNAst(SourceRef sourceRef, Object value) {
        super();
        this.sourceRef = sourceRef;
        this.value = value;
    }

    @Override
    public Object evaluate(Map<String, Object> model) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public SourceRef getSourceRef() {
        return sourceRef;
    }

}