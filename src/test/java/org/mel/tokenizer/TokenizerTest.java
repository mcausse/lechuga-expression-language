package org.mel.tokenizer;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenizerTest {

    @Test
    public void testName() throws Exception {
        ExpressionTokenizer t = new ExpressionTokenizer();

        {
            List<Token> ts = t.tokenizeAsList("immediate", "1 '2' true null not [] () +", 1, 1);
            assertEquals(
                    "[NUM:1.0, STR:2, BOOL:true, NULL:null, SYM:not, OPEN_CLAU:[, CLOSE_CLAU:], OPEN_PARENTESIS:(, CLOSE_PARENTESIS:), SYM:+]",
                    ts.toString());
        }
        {
            List<Token> ts = t.tokenizeAsList("test", "model.dogs[1]['jou']", 1, 1);
            assertEquals(
                    "[SYM:model, SYM:., SYM:dogs, OPEN_CLAU:[, NUM:1.0, CLOSE_CLAU:], OPEN_CLAU:[, STR:jou, CLOSE_CLAU:]]",
                    ts.toString());
        }
        {
            List<Token> ts = t.tokenizeAsList("test", "not model.dogs[not 1]['jou']", 1, 1);
            assertEquals(
                    "[SYM:not, SYM:model, SYM:., SYM:dogs, OPEN_CLAU:[, SYM:not, NUM:1.0, CLOSE_CLAU:], OPEN_CLAU:[, STR:jou, CLOSE_CLAU:]]",
                    ts.toString());
        }
    }
}
