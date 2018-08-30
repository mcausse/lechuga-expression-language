package org.mel.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mel.ExpressionEvaluator;
import org.mel.parser.ExpressionParser;
import org.mel.parser.ExpressionParser.Ast;
import org.mel.tokenizer.ExpressionTokenizer;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.Token;
import org.mel.tokenizer.TokenException;

public class ExpressionParserTest {

    final ExpressionParser ep = new ExpressionParser();

    @Test
    public void testName() throws Exception {

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

    @Test
    public void testName2() throws Exception {

        ExpressionTokenizer t = new ExpressionTokenizer();

        {
            List<Token> ts = t.tokenize("test", "1+2*3+4", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("11.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1 eq 1 and (1+1 eq 2 or false)", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1 eq 1 and (1+2 eq 2 or false)", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("false", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "null eq null", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "'jou'=='juas' || 'a' == 'a'", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "not false and not(not true)", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "null", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertNull(ast.evaluate(null));
        }
        {
            List<Token> ts = t.tokenize("test", "'jou'", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("jou", ast.evaluate(null));
        }

        ///////////////////

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");

            List<Token> ts = t.tokenize("test", "name", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("mhc", ast.evaluate(model).toString());
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", Arrays.asList("mhc", "mem"));

            List<Token> ts = t.tokenize("test", "name[1]", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("mem", ast.evaluate(model).toString());
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("name", "mhc");

            List<Token> ts = t.tokenize("test", "name.class.simpleName.trim.toUpperCase", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("STRING", ast.evaluate(model).toString());
        }

        {
            Map<String, Object> model = new HashMap<>();
            model.put("age", 32);

            List<Token> ts = t.tokenize("test", "age>=32&&age<=45", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(model).toString());
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("alive", true);

            List<Token> ts = t.tokenize("test", "alive&&true", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(model).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "long 1.2", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("1", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "int 1", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("1", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "int(1+1)", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("2", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "(int 1)+1", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("2.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "(int 3.14159)", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("3", ast.evaluate(null).toString());
        }

        {
            List<Token> ts = t.tokenize("test", "-1+-1", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("-2.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "1---1", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("0.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "-(-(-1))", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("-1.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "---1", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("-1.0", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "null eq null", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "null eq null && true || false==true", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("true", ast.evaluate(null).toString());
        }

        {
            Map<String, Object> model = new HashMap<>();
            Map<String, Object> model2 = new HashMap<>();
            model2.put("b", "jou!");
            model.put("a", model2);

            List<Token> ts = t.tokenize("test", "a.b", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("jou!", ast.evaluate(model).toString());
        }
        {
            Map<String, Object> model = new HashMap<>();
            Map<String, Object> model2 = new HashMap<>();
            model2.put("b", "jou!");
            model.put("a", model2);

            List<Token> ts = t.tokenize("test", "a['b']", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("jou!", ast.evaluate(model).toString());
        }
        {
            Map<String, Object> model = new HashMap<>();
            model.put("a", new Integer[][] { { 1, 2 }, { 3, 4 } });

            List<Token> ts = t.tokenize("test", "a[1][0]", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("3", ast.evaluate(model).toString());
        }
        {
            List<Token> ts = t.tokenize("test", "'jou'", 1, 1);
            Ast ast = ep.parseExpression(ts);
            assertEquals("jou", ast.evaluate(null).toString());
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
