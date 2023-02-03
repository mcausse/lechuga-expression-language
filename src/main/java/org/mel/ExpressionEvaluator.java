package org.mel;

import org.mel.parser.ExpressionParser;
import org.mel.parser.ast.Ast;
import org.mel.tokenizer.ExpressionTokenizer;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.Token;
import org.mel.tokenizer.TokenIterator;

import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

    final ExpressionTokenizer t = new ExpressionTokenizer();
    final ExpressionParser ep = new ExpressionParser();

    public Object evaluate(SourceRef sourceRef, String expression, Map<String, Object> model) {
//        List<Token> ts = t.tokenize(sourceRef.getSourceFile(), expression, sourceRef.getLine(), sourceRef.getColumn());
        TokenIterator<Token> ts=t.tokenize(sourceRef.getSourceFile(), expression, sourceRef.getLine(), sourceRef.getColumn());
        Ast expressionAst = ep.parseExpression(ts);
        return expressionAst.evaluate(model);
    }
}
