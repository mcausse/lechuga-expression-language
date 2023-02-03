package org.mel.tokenizer;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.mel.tokenizer.ExpressionTokenizer;
import org.mel.tokenizer.Token;
import org.mel.tokenizer.TokenIterator;

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

    }
}
