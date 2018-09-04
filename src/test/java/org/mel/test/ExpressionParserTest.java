package org.mel.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mel.ExpressionEvaluator;
import org.mel.parser.Ast;
import org.mel.parser.ExpressionParser;
import org.mel.tokenizer.ExpressionTokenizer;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.Token;
import org.mel.tokenizer.TokenException;

public class ExpressionParserTest {

    final ExpressionParser ep = new ExpressionParser();

    @Test
    public void testAstToString() throws Exception {

        ExpressionTokenizer t = new ExpressionTokenizer();

        {
            List<Token> ts = t.tokenize("test", "model.dogs[1]['jou']", 1, 1);
            assertEquals(
                    "[SYM:model, SYM:., SYM:dogs, OPEN_CLAU:[, NUM:1.0, CLOSE_CLAU:], OPEN_CLAU:[, STR:jou, CLOSE_CLAU:]]",
                    ts.toString());
            assertEquals("(model.dogs[1.0][jou])", ep.parseExpression(ts).toString());
        }

        {
            List<Token> ts = t.tokenize("test", "not model.dogs[not 1]['jou']", 1, 1);
            assertEquals(
                    "[SYM:not, SYM:model, SYM:., SYM:dogs, OPEN_CLAU:[, SYM:not, NUM:1.0, CLOSE_CLAU:], OPEN_CLAU:[, STR:jou, CLOSE_CLAU:]]",
                    ts.toString());
            assertEquals("(not (model.dogs[(not 1.0)][jou]))", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1*2 % 3", 1, 1);
            assertEquals("(1.0 * 2.0 % 3.0)", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1*not 2%3", 1, 1);
            assertEquals("(1.0 * (not 2.0) % 3.0)", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1+2*3+4", 1, 1);
            assertEquals("(1.0 + (2.0 * 3.0) + 4.0)", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "true eq not false", 1, 1);
            assertEquals("(true eq (not false))", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "true and 1 eq 1 or false", 1, 1);
            assertEquals("((true and (1.0 eq 1.0)) or false)", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "true and (1 eq 1 or false)", 1, 1);
            assertEquals("(true and ((1.0 eq 1.0) or false))", ep.parseExpression(ts).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "2*(3+4-1)", 1, 1);
            assertEquals("(2.0 * (3.0 + 4.0 - 1.0))", ep.parseExpression(ts).toString());
        }
    }

    void markdownTableHeader() {

        System.out.println("Model | Expression | Result");
        System.out.println("--- | --- | ---");
    }

    void eval(Object expectedResult, String expression, Map<String, Object> model) {
        ExpressionTokenizer t = new ExpressionTokenizer();
        List<Token> ts = t.tokenize("test", expression, 1, 1);
        Ast ast = ep.parseExpression(ts);
        assertEquals(expectedResult, ast.evaluate(model));

        if (model == null) {
            System.out.print(" | ");
        } else {
            System.out.print(" | " + model);
        }
        System.out.print(" | " + expression.trim());
        if (expectedResult == null) {
            System.out.print(" | " + expectedResult);
        } else {
            System.out.print(" | " + expectedResult + " (" + expectedResult.getClass().getSimpleName() + ")");
        }
        System.out.println();
    }

    @Test
    public void testNameEvaluate() throws Exception {

        markdownTableHeader();

        ExpressionTokenizer t = new ExpressionTokenizer();

        eval(11.0, "1+2*3+4", null);
        eval(true, "1 eq 1 and (1+1 eq 2 or false)", null);
        eval(false, "1 eq 1 and (1+2 eq 2 or false)", null);
        eval(true, "null eq null", null);
        eval(true, "'jou'=='juas' || 'a' == 'a'", null);
        eval(true, "not false and not(not true)", null);
        eval(null, "null", null);
        eval("jou", "'jou'", null);

        eval(3L, "long (10/3)", null);
        eval(3L, "long 10.9 / long 3.27", null);

        eval(true, "10%3==1", null);

        eval("Short", "typeof short 3", null);
        eval("Integer", "typeof int 3", null);
        eval("Double", "typeof 3", null);
        eval("Double", "typeof 3.4", null);
        eval("String", "typeof 'jou'", null);
        eval("Boolean", "typeof true", null);
        eval("Boolean", "typeof false", null);
        eval("null", "typeof null", null);

        eval(null, "", null);

        ///////////////////

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");

            List<Token> ts = t.tokenize("test", "name", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("mhc", ast.evaluate(model).toString());
            eval("mhc", "name", model);
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", Arrays.asList("mhc", "mem"));
            model.put("i", 1);

            eval("mem", "name[1]", model);
            eval("mem", "name[i]", model);
            eval("alo mhc!", "'alo '+name[i/2]+'!'", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");
            eval("STRING", "name.class.simpleName.trim.toUpperCase", model);
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("age", 32);

            eval(true, "age>=32&&age<=45", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("alive", true);

            eval(true, "alive&&true", model);
        }

        eval(1L, "long 1.2", null);
        eval(1, "int 1", null);
        eval(2, "int(1+1)", null);
        eval(2.0, "(int 1)+1", null);
        eval(2, "(int 1)+int 1", null);
        eval(3, "(int(3.14159))", null);
        eval(314, "(int(3.14159*100))", null);
        eval(-2.0, "-1+-1", null);
        eval(0.0, "1---1", null);
        eval(-1.0, "-(-(-1))", null);
        eval(-1.0, "---1", null);
        eval(true, "null eq null", null);
        eval(true, "null eq null && true || false==true", null);

        eval(true, "'jou' eq 'jou'", null);
        eval(false, "'jou ' eq 'jou'", null);
        eval("jou", " 'jou' ", null);
        eval("joujuas", " 'jou'+'juas' ", null);
        eval((short) 5, " short 2 + short 3", null);
        eval(5, " int 2 + int 3", null);
        eval(5L, " long 2 + long 3", null);
        eval(5.0f, " float 2 + float 3", null);
        eval(5.0, " double 2 + double 3", null);
        eval("3", " string int long 3.14159", null);

        eval("ko", " null?'ok':'ko'", null);
        eval("ok", " true?'ok':'ko'", null);
        eval("ko", " false?'ok':'ko'", null);
        eval("ok", " 1?'ok':'ko'", null);
        eval("ko", " 0?'ok':'ko'", null);
        eval("ok", " 'a'?'ok':'ko'", null);
        eval("ko", " ''?'ok':'ko'", null);

        eval("is ok", " 3+3==3*2?'is'+' '+'ok':null", null);
        eval(null, " 3%3!=0?true:null", null);

        eval(14.0, " 1+3*2+2*3+1", null);

        // // TODO
        // eval("STRING", "'a'.class.simpleName.trim.toUpperCase", null);
        // eval("INTEGER", "3.class.simpleName.trim.toUpperCase", null);

        eval(3, "3->intValue()", null);
        eval(3.0f, "3->floatValue()", null);
        eval("cde", "'abcdefg'->substring(int 2, int 5)", null);
        assertEquals("cde", "abcdefg".substring(2, 5));

        eval("STRING", "'a'->getClass()->getSimpleName()->trim()->toUpperCase()", null);
        eval("INTEGER", "(int 3)->getClass()->getSimpleName()->trim()->toUpperCase()", null);

        eval(6, "3->intValue() * 2->shortValue()", null);

        eval(3f, "(int 3)->floatValue()", null);

        {
            String exp = " long(c1 ? (c2?1:2) : (c3?3:4))";

            Map<String, Object> model = new HashMap<>();
            model.put("c1", true);
            model.put("c2", true);
            model.put("c3", true);

            eval(1L, exp, model);
            model.put("c2", false);
            eval(2L, exp, model);

            model.put("c1", false);
            eval(3L, exp, model);

            model.put("c3", false);
            eval(4L, exp, model);
        }

        {
            Map<String, Object> model = new HashMap<>();
            Map<String, Object> model2 = new HashMap<>();
            model2.put("b", "jou!");
            model.put("a", model2);

            eval("jou!", "a['b']", model);
            eval("jou!", "a.b", model);
            eval(((Map<?, ?>) model.get("a")).keySet(), "keys a", model);
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("a", new Integer[][] { { 1, 2 }, { 3, 4 } });

            eval(3, "a[1][0]", model);
            eval(Arrays.asList(0, 1), "keys a", model);
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

        assertEquals("true", e.evaluate(s, "'jou' eq 'jou'", null).toString());
        assertEquals("jou", e.evaluate(s, " 'jou' ", null));
        assertEquals("joujuas", e.evaluate(s, " 'jou'+'juas' ", null));

        assertEquals("5", e.evaluate(s, " short 2 + short 3", null).toString());
        assertEquals("5", e.evaluate(s, " int 2 + int 3", null).toString());
        assertEquals("5", e.evaluate(s, " long 2 + long 3", null).toString());
        assertEquals("5.0", e.evaluate(s, " float 2 + float 3", null).toString());
        assertEquals("5.0", e.evaluate(s, " double 2 + double 3", null).toString());

        assertEquals("3", e.evaluate(s, " string int long 3.14159", null));
    }
}
