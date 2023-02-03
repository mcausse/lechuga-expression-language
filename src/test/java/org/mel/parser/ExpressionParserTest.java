package org.mel.parser;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mel.ExpressionEvaluator;
import org.mel.parser.ast.Ast;
import org.mel.tokenizer.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ExpressionParserTest {

    final ExpressionParser ep = new ExpressionParser();

    static Stream<Arguments> testAstToStringProvider() {
        return Stream.of(
                Arguments.of("model.dogs[1]['jou']", "(model.dogs[1.0][jou])"),
                Arguments.of("not model.dogs[not 1]['jou']", "(not (model.dogs[(not 1.0)][jou]))"),
                Arguments.of("1*2 % 3", "(1.0 * 2.0 % 3.0)"),
                Arguments.of("1*not 2%3", "(1.0 * (not 2.0) % 3.0)"),
                Arguments.of("1+2*3+4", "(1.0 + (2.0 * 3.0) + 4.0)"),
                Arguments.of("true eq not false", "(true eq (not false))"),
                Arguments.of("true and 1 eq 1 or false", "((true and (1.0 eq 1.0)) or false)"),
                Arguments.of("true and (1 eq 1 or false)", "(true and ((1.0 eq 1.0) or false))"),
                Arguments.of("2*(3+4-1)", "(2.0 * (3.0 + 4.0 - 1.0))")
        );
    }

    @ParameterizedTest
    @MethodSource("testAstToStringProvider")
    void testAstToString(String inputExpression, String expectedStringAst) {
        ExpressionTokenizer t = new ExpressionTokenizer();
            TokenIterator<Token> ti = t.tokenize("test", inputExpression, 1, 1);

            // Act
            var str = ep.parseExpression(ti).toString();

            assertThat(str).isEqualTo(expectedStringAst);
    }

    void eval(Object expectedResult, String expression, Map<String, Object> model) {
        ExpressionTokenizer t = new ExpressionTokenizer();
        TokenIterator<Token> ti = t.tokenize("test", expression, 1, 1);
        Ast ast = ep.parseExpression(ti);
        assertEquals(expectedResult, ast.evaluate(model));

        if (model != null) {
            System.out.println(model);
        }
        System.out.println(expression.trim());
        System.out.println("=> " + expectedResult);
        System.out.println();
    }

    @Test
    public void testNameEvaluate() throws Exception {

        ExpressionTokenizer t = new ExpressionTokenizer();

        eval(11.0, "1+2*3+4", null);
        eval(true, "1 eq 1 and (1+1 eq 2 or false)", null);
        eval(false, "1 eq 1 and (1+2 eq 2 or false)", null);
        eval(true, "null eq null", null);
        eval(true, "'jou'=='juas' || 'a' == 'a'", null);
        eval(true, "not false and not(not true)", null);
        eval(null, "null", null);
        eval("jou", "'jou'", null);
        eval("jou", ":jou", null);

        ///////////////////

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");

            TokenIterator<Token> ti = t.tokenize("test", "name", 1, 1);
            Ast ast = ep.parseExpression(ti);
            assertEquals("mhc", ast.evaluate(model).toString());
            eval("mhc", "name", model);
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", Arrays.asList("mhc", "mem"));

            TokenIterator<Token> ti = t.tokenize("test", "name[1]", 1, 1);
            Ast ast = ep.parseExpression(ti);
            assertEquals("mem", ast.evaluate(model).toString());
            eval("mem", "name[1]", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");

            TokenIterator<Token> ti = t.tokenize("test", "name.class.simpleName.trim.toUpperCase", 1, 1);
            Ast ast = ep.parseExpression(ti);
            assertEquals("STRING", ast.evaluate(model).toString());
            eval("STRING", "name.class.simpleName.trim.toUpperCase", model);
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("age", 32);

            TokenIterator<Token> ti = t.tokenize("test", "age>=32&&age<=45", 1, 1);
            Ast ast = ep.parseExpression(ti);
            assertEquals("true", ast.evaluate(model).toString());
            eval(true, "age>=32&&age<=45", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("alive", true);

            TokenIterator<Token> ti = t.tokenize("test", "alive&&true", 1, 1);
            Ast ast = ep.parseExpression(ti);
            assertEquals("true", ast.evaluate(model).toString());
            eval(true, "alive&&true", model);
        }

        eval(1L, "long 1.2", null);
        eval(1, "int 1", null);
        eval(2, "int(1+1)", null);
        eval(2.0, "(int 1)+1", null);
        eval(3, "(int 3.14159)", null);
        eval(-2.0, "-1+-1", null);
        eval(0.0, "1---1", null);
        eval(-1.0, "-(-(-1))", null);
        eval(-1.0, "---1", null);
        eval(true, "null eq null", null);
        eval(true, "null eq null && true || false==true", null);

        eval(true, ":jou eq 'jou'", null);
        eval("jou", " :jou ", null);
        eval("joujuas", " :jou+:juas ", null);
        eval((short) 5, " short 2 + short 3", null);
        eval(5, " int 2 + int 3", null);
        eval(5L, " long 2 + long 3", null);
        eval(5.0f, " float 2 + float 3", null);
        eval(5.0, " double 2 + double 3", null);
        eval("3", " string int long 3.14159", null);

        {
            Map<String, Object> model = new HashMap<>();
            Map<String, Object> model2 = new HashMap<>();
            model2.put("b", "jou!");
            model.put("a", model2);

            eval("jou!", "a['b']", model);
            eval("jou!", "a.b", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("a", new Integer[][]{{1, 2}, {3, 4}});

            eval(3, "a[1][0]", model);
        }

        {
            try {
                t.tokenize("test", "'jou", 1, 1);
                fail();
            } catch (TokenException e) {
                assertEquals("[at test:1,1]: expected closing \"'\"", e.getMessage());
            }
        }
        {
            try {
                t.tokenize("test", "3 * 2 && true || 'ju\nas' + 'jou", 1, 1);
                fail();
            } catch (TokenException e) {
                assertEquals("[at test:2,7]: expected closing \"'\"", e.getMessage());
            }
        }
        {
            try {
                t.tokenize("test", "3 * 2 && true \n|| 'ju\nas' + 'jou", 1, 1);
                fail();
            } catch (TokenException e) {
                assertEquals("[at test:3,7]: expected closing \"'\"", e.getMessage());
            }
        }

        ExpressionEvaluator e = new ExpressionEvaluator();
        SourceRef s = new SourceRef("test", 1, 1);

        assertEquals("true", e.evaluate(s, ":jou eq 'jou'", null).toString());
        assertEquals("jou", e.evaluate(s, " :jou ", null));
        assertEquals("joujuas", e.evaluate(s, " :jou+:juas ", null));

        assertEquals("5", e.evaluate(s, " short 2 + short 3", null).toString());
        assertEquals("5", e.evaluate(s, " int 2 + int 3", null).toString());
        assertEquals("5", e.evaluate(s, " long 2 + long 3", null).toString());
        assertEquals("5.0", e.evaluate(s, " float 2 + float 3", null).toString());
        assertEquals("5.0", e.evaluate(s, " double 2 + double 3", null).toString());

        assertEquals("3", e.evaluate(s, " string int long 3.14159", null));
    }
}
