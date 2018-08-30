package org.mel.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionTokenizer {

    public List<Token> tokenize(String sourceFile, String text, int line, int column) {

        List<Token> r = new ArrayList<>();

        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                if (c == '\n') {
                    column = 1;
                    line++;
                } else {
                    column++;
                }
                i++;
                continue;
            }

            int openingLine = line;
            int openingColumn = column;

            switch (c) {
            case '(':
                r.add(new Token(EToken.OPEN_PARENTESIS, "(", new SourceRef(sourceFile, line, column)));
                column++;
                i++;
                break;
            case ')':
                r.add(new Token(EToken.CLOSE_PARENTESIS, ")", new SourceRef(sourceFile, line, column)));
                column++;
                i++;
                break;
            case '[':
                r.add(new Token(EToken.OPEN_CLAU, "[", new SourceRef(sourceFile, line, column)));
                column++;
                i++;
                break;
            case ']':
                r.add(new Token(EToken.CLOSE_CLAU, "]", new SourceRef(sourceFile, line, column)));
                column++;
                i++;
                break;
            case ':': {

                i++; // chupa :
                column++;

                int start = i;
                while (i < text.length() && Character.isJavaIdentifierPart(text.charAt(i))) {
                    column++;
                    i++;
                }
                String value = text.substring(start, i);
                r.add(new Token(EToken.STR, value, new SourceRef(sourceFile, openingLine, openingColumn)));
                break;
            }
            case '\'': {

                i++; // chupa '
                column++;

                int start = i;
                while (i < text.length() && text.charAt(i) != '\'') {
                    if (text.charAt(i) == '\n') {
                        line++;
                        column = 1;
                    } else {
                        column++;
                    }
                    i++;
                }

                if (i >= text.length() || text.charAt(i) != '\'') {
                    throw new TokenException(new SourceRef(sourceFile, openingLine, openingColumn),
                            "expected closing \"'\"");
                }
                int end = i;
                i++; // chupa '
                column++;

                String value = text.substring(start, end);
                r.add(new Token(EToken.STR, value, new SourceRef(sourceFile, openingLine, openingColumn)));
                break;
            }
            default:

            {
                {
                    int p = i;

                    int digits = 0;

                    // if (code.charAt(p) == '+' || code.charAt(p) == '-') {
                    // p++;
                    // }

                    while (p < text.length() && Character.isDigit(text.charAt(p))) {
                        digits++;
                        p++;
                    }

                    if (p < text.length() && text.charAt(p) == '.') {
                        p++; // chupa .
                    }

                    while (p < text.length() && Character.isDigit(text.charAt(p))) {
                        digits++;
                        p++;
                    }

                    if (digits > 0) {
                        String value = text.substring(i, i + digits);
                        double v = Double.parseDouble(value);
                        r.add(new Token(EToken.NUM, v, new SourceRef(sourceFile, openingLine, openingColumn)));
                        i = p;
                        column += digits;
                        continue;
                    }
                }
                {
                    List<Character> cs1 = Arrays.asList('.', '+', '-', '*', '/', '%', '>', '<');
                    List<String> cs2 = Arrays.asList("==", "!=", "<>", ">=", "<=", "&&", "||", "/*", "*/");

                    if (i + 1 < text.length()) {
                        String c2 = text.substring(i, i + 2);
                        if (cs2.contains(c2)) {
                            r.add(new Token(EToken.SYM, text.substring(i, i + 2),
                                    new SourceRef(sourceFile, openingLine, openingColumn)));
                            column += 2;
                            i += 2;
                            continue;
                        }
                    }

                    if (cs1.contains(text.charAt(i))) {
                        r.add(new Token(EToken.SYM, text.substring(i, i + 1),
                                new SourceRef(sourceFile, openingLine, openingColumn)));
                        column++;
                        i++;
                        continue;
                    }
                }
            } {
                int start = i;
                while (i < text.length() && Character.isJavaIdentifierPart(text.charAt(i))) {
                    column++;
                    i++;
                }
                if (i > start) {
                    String value = text.substring(start, i);
                    if (value.equals("null")) {
                        r.add(new Token(EToken.NULL, null, new SourceRef(sourceFile, openingLine, openingColumn)));
                    } else if (value.equals("true")) {
                        r.add(new Token(EToken.BOOL, true, new SourceRef(sourceFile, openingLine, openingColumn)));
                    } else if (value.equals("false")) {
                        r.add(new Token(EToken.BOOL, false, new SourceRef(sourceFile, openingLine, openingColumn)));
                    } else {
                        r.add(new Token(EToken.SYM, value, new SourceRef(sourceFile, openingLine, openingColumn)));
                    }
                    continue;
                }
            }

                throw new TokenException(new SourceRef(sourceFile, openingLine, openingColumn),
                        "unexpected: '" + text.charAt(i));
            }
        }

        return r;
    }

}
