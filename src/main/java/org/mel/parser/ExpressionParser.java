package org.mel.parser;

import org.mel.parser.ast.*;
import org.mel.tokenizer.*;

import java.util.ArrayList;
import java.util.List;

//*      <exp>  ::= <exp1> {<op-or> <exp1>}             (menys prioritari!)
//*      <exp1> ::= <exp2> {<op-and> <exp2>}
//*      <exp2> ::= <exp3> [<op-relational> <exp3>]
//*      <exp3> ::= <exp4> {<op-addsub> <exp4>}
//*      <exp4> ::= <exp5> {<op-muldivmod> <exp5>}
//*      <exp5> ::= {"not"|"int"|"float"|...|"-"} <exp6>
//*      <exp6> ::= <expN> {"[" <exp> "]"}
//*      <expN> ::= "(" <exp> ")" | <var> | <num> | <string> | <boolean> | "null"
//*      <var>  ::= IDENT {"." IDENT}
public class ExpressionParser {

    /**
     * Parses the input {@link Token}s to an Abstract-Syntax Tree (AST) type.
     *
     * @param tokensIterator the input {@link Token}s collection.
     * @return the parsed Abstract-Syntax Tree (AST)
     */
    public Ast parseExpression(TokenIterator<Token> tokensIterator) {
        Token t = tokensIterator.current();
        try {
            Ast r = parseRelationalOr(tokensIterator);
            if (tokensIterator.notEof()) {
                throw new RuntimeException("grammar exception");
            }
            return r;
        } catch (Exception e) {
            throw new TokenException(t.getSourceRef(), "evaluating expression: " + t, e);
        }
    }

    protected Ast parseRelationalOr(TokenIterator<Token> i) {
        Ast left = parseRelationalAnd(i);

        List<String> ops = new ArrayList<>();
        List<Ast> right = new ArrayList<>();

        while (i.notEof() && i.current().getType() == EToken.SYM &&
                /**/(i.current().getValue().equals("or") || i.current().getValue().equals("||"))) {
            String op = (String) i.current().getValue();
            ops.add(op);
            i.next(); // chupa and
            if (!i.notEof()) {
                throw new RuntimeException("expected second operand for: " + op);
            }
            right.add(parseRelationalAnd(i));
        }

        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseRelationalAnd(TokenIterator<Token> i) {
        Ast left = parseBinaryRelationalOperation(i);

        List<String> ops = new ArrayList<>();
        List<Ast> right = new ArrayList<>();

        while (i.notEof() && i.current().getType() == EToken.SYM &&
                /**/(i.current().getValue().equals("and") || i.current().getValue().equals("&&"))) {
            String op = (String) i.current().getValue();
            ops.add(op);
            i.next(); // chupa and
            if (!i.notEof()) {
                throw new RuntimeException("expected second operand for: " + op);
            }
            right.add(parseBinaryRelationalOperation(i));
        }

        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseBinaryRelationalOperation(TokenIterator<Token> i) {
        Ast left = parseBinaryAddSub(i);
        String op = null;
        Ast right = null;

        if (i.hasNext() && i.current().getType() == EToken.SYM && (
                /**/i.current().getValue().equals("eq") || i.current().getValue().equals("==") ||
                /**/i.current().getValue().equals("ne") || i.current().getValue().equals("!=") ||
                /**/i.current().getValue().equals("le") || i.current().getValue().equals("<=") ||
                /**/i.current().getValue().equals("ge") || i.current().getValue().equals(">=") ||
                /**/i.current().getValue().equals("lt") || i.current().getValue().equals("<") ||
                /**/i.current().getValue().equals("gt") || i.current().getValue().equals(">"))) {
            op = (String) i.current().getValue();
            i.next(); // chupa op
            if (!i.notEof()) {
                throw new RuntimeException("expected second operand for: " + op);
            }
            right = parseBinaryAddSub(i);
        }

        return new SingleBinaryAst(left, op, right);
    }

    protected Ast parseBinaryAddSub(TokenIterator<Token> i) {
        Ast left = parseBinaryMulDivMod(i);

        List<String> ops = new ArrayList<>();
        List<Ast> right = new ArrayList<>();

        while (i.notEof() && i.current().getType() == EToken.SYM && (
                /**/i.current().getValue().equals("+") ||
                /**/i.current().getValue().equals("-"))) {
            String op = (String) i.current().getValue();
            ops.add(op);
            i.next(); // chupa +-
            if (!i.notEof()) {
                throw new RuntimeException("expected second operand for: " + op);
            }
            right.add(parseBinaryMulDivMod(i));
        }

        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseBinaryMulDivMod(TokenIterator<Token> i) {
        Ast left = parseUnaryOperation(i);

        List<String> ops = new ArrayList<>();
        List<Ast> right = new ArrayList<>();

        while (i.notEof() && i.current().getType() == EToken.SYM && (
                /**/i.current().getValue().equals("*") ||
                /**/i.current().getValue().equals("/") ||
                /**/i.current().getValue().equals("%"))) {
            String op = (String) i.current().getValue();
            ops.add(op);
            i.next(); // chupa */%
            if (!i.notEof()) {
                throw new RuntimeException("expected second operand for: " + op);
            }
            right.add(parseUnaryOperation(i));
        }

        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseUnaryOperation(TokenIterator<Token> i) {
        List<String> ops = new ArrayList<>();
        while (i.current().getType() == EToken.SYM && (
                /**/i.current().getValue().equals("not") ||
                /**/i.current().getValue().equals("byte") ||
                /**/i.current().getValue().equals("short") ||
                /**/i.current().getValue().equals("int") ||
                /**/i.current().getValue().equals("long") ||
                /**/i.current().getValue().equals("float") ||
                /**/i.current().getValue().equals("double") ||
                /**/i.current().getValue().equals("string") ||
                /**/i.current().getValue().equals("-")
                /**/)) {
            ops.add((String) i.current().getValue());
            i.next(); // chupa op
        }
        Ast right = parseIndexedOperation(i);

        return new UnaryAst(ops, right);
    }

    protected Ast parseIndexedOperation(TokenIterator<Token> i) {
        Ast left = parseExpNAst(i);
        List<Ast> right = new ArrayList<>();
        while (i.hasNext() && i.current().getType() == EToken.OPEN_CLAU) {
            i.next(); // chupa [

            right.add(parseRelationalOr(i));

            if (i.current().getType() != EToken.CLOSE_CLAU) {
                throw new TokenException(i.current().getSourceRef(),
                        "expected " + EToken.OPEN_CLAU + ", but readed " + i.current());
            }
            i.next(); // chupa ]
        }
        return new Exp6Ast(left, right);
    }

    protected Ast parseExpNAst(TokenIterator<Token> i) {
        EToken type = i.current().getType();
        switch (type) {
            case OPEN_PARENTESIS: {
                i.next();// chupa (
                Ast r = parseRelationalOr(i);
                i.next();// chupa )
                return r;
            }
            case SYM:
                return parseVar(i);
            case NUM:
            case STR:
            case BOOL:
            case NULL:
                ExpNAst r = new ExpNAst(i.current().getSourceRef(), i.current().getValue());
                i.next(); // chupa
                return r;
            default:
                throw new TokenException(i.current().getSourceRef(), "unexpected token: " + i.current());
        }
    }

    public VarAst parseVar(TokenIterator<Token> i) {
        List<String> idents = new ArrayList<>();
        Token c = i.current();
        SourceRef sourceRef = c.getSourceRef();
        if (c.getType() != EToken.SYM) {
            throw new TokenException(i.current().getSourceRef(), "unexpected token: " + i.current());
        }
        idents.add((String) c.getValue());
        i.next();

        while (i.hasNext() && i.current().getType() == EToken.SYM && i.current().getValue().equals(".")) {
            i.next(); // chupa .

            c = i.current();
            if (c.getType() != EToken.SYM) {
                throw new TokenException(i.current().getSourceRef(), "unexpected token: " + i.current());
            }
            idents.add((String) c.getValue());
            i.next();
        }

        return new VarAst(sourceRef, idents);
    }

}
