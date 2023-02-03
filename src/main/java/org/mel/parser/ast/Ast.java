package org.mel.parser.ast;

import org.mel.tokenizer.SourceRef;

import java.util.Map;

public interface Ast {

    Object evaluate(Map<String, Object> model);

    SourceRef getSourceRef();
}