package org.mel.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.mel.tokenizer.ExpressionTokenizer;
import org.mel.tokenizer.Token;

public class TokenizerTest {

    @Test
    public void testName() throws Exception {
        ExpressionTokenizer t = new ExpressionTokenizer();

        {
            List<Token> ts = t.tokenize("immediate", "1 '2' true null not [] () +", 1, 1);
            assertEquals(
                    "[NUM:1.0, STR:2, BOOL:true, NULL:null, SYM:not, OPEN_CLAU:[, CLOSE_CLAU:], OPEN_PARENTESIS:(, CLOSE_PARENTESIS:), SYM:+]",
                    ts.toString());
        }

    }
}
