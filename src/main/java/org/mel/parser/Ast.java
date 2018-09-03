package org.mel.parser;

import java.util.Map;

import org.mel.tokenizer.SourceRef;

public interface Ast {

    Object evaluate(Map<String, Object> model);

    SourceRef getSourceRef();

}