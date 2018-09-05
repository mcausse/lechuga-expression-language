package org.mel.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.mel.tokenizer.EToken;
import org.mel.tokenizer.SourceRef;
import org.mel.tokenizer.Token;
import org.mel.tokenizer.TokenException;
import org.mel.tokenizer.TokenIterator;

//*      <exp>  ::= <exp0> ["?" <exp0> ":" <exp0>]                                   (menys prioritari!)
//*      <exp0> ::= <exp1> {<op-or> <exp1>}
//*      <exp1> ::= <exp2> {<op-and> <exp2>}
//*      <exp2> ::= <exp3> [<op-relational> <exp3>]
//*      <exp3> ::= <exp4> {<op-addsub> <exp4>}
//*      <exp4> ::= <exp5> {<op-muldivmod> <exp5>}
//*      <exp5> ::= {<unary-op>} <exp6>
//*      <exp6> ::= <expN> {"[" <exp> "]"}
//*      <exp7> ::= <expN> { "->" IDENT "(" [ <exp> {"," <exp>}] ")" }
//*      <expN> ::= "(" <exp> ")" | <var> | NUM | STRING | BOOL | "null"
//*      <var>  ::= IDENT {"." IDENT}
//*
//*      <op-or>            ::= "or" | "||"
//*      <op-and>           ::= "and" | "&&"
//*      <op-relational>    ::= "eq" | "==" | "ne" | "!=" | "le" | "<=" | "ge" | ">=" | "lt" | "<" | "gt" | ">"
//*      <op-addsub>        ::= "+" | "-"
//*      <op-muldivmod>     ::= "*" | "/" | "%"
//*      <unary-op>         ::= "not" | "byte" | "short" | "int" | "long" |
//*                             "float" | "double" | "string" | "keys" | "typeof" | "-"
//*
//*
//*      <exp6> ::= <exp7> { "->" IDENT "(" [ <exp> {"," <exp>}] ")" }
//*      <exp7> ::= <expN> {"[" <exp> "]"}
//*
//*
public class ExpressionParser {

    public Ast parseExpression(List<Token> expressionTokens) {
        if (expressionTokens.isEmpty()) {
            return new ExpNAst(null, null);
        }
        TokenIterator<Token> i = new TokenIterator<>(expressionTokens);
        Token t = i.current();
        try {
            Ast r = parseExpression(i);
            if (i.notEof()) {
                throw new RuntimeException("grammar exception: EOF not reached at the end of expression: " + r);
            }
            return r;
        } catch (Exception e) {
            throw new TokenException(t.getSourceRef(), "evaluating expression: " + expressionTokens, e);
        }
    }

    public Ast parseExpression(TokenIterator<Token> i) {
        return parseExp(i);
    }

    protected Ast parseExp(TokenIterator<Token> i) {
        Ast r = parseExp0Ast(i);
        if (i.hasNext() && i.current().getType() == EToken.SYM && i.current().getValue().equals("?")) {
            i.next(); // chupa ?
            Ast thenAst = parseExp0Ast(i);
            if (i.hasNext() && i.current().getType() == EToken.SYM && i.current().getValue().equals(":")) {
                i.next(); // chupa :
                Ast elseAst = parseExp0Ast(i);
                return new TernaryAst(r, thenAst, elseAst);
            }
            throw new RuntimeException("expected : after ? " + i.current());
        } else {
            return r;
        }
    }

    protected Ast parseExp0Ast(TokenIterator<Token> i) {
        Ast left = parseExp1Ast(i);

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
            right.add(parseExp1Ast(i));
        }

        if (ops.isEmpty()) {
            return left;
        }
        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseExp1Ast(TokenIterator<Token> i) {
        Ast left = parseExp2Ast(i);

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
            right.add(parseExp2Ast(i));
        }

        if (ops.isEmpty()) {
            return left;
        }
        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseExp2Ast(TokenIterator<Token> i) {
        Ast left = parseExp3Ast(i);
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
            right = parseExp3Ast(i);
        }

        if (op == null) {
            return left;
        }
        return new SingleBinaryAst(left, op, right);
    }

    protected Ast parseExp3Ast(TokenIterator<Token> i) {
        Ast left = parseExp4Ast(i);

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
            right.add(parseExp4Ast(i));
        }

        if (ops.isEmpty()) {
            return left;
        }
        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseExp4Ast(TokenIterator<Token> i) {
        Ast left = parseExp5Ast(i);

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
            right.add(parseExp5Ast(i));
        }

        if (ops.isEmpty()) {
            return left;
        }
        return new MultipleBinaryAst(left, ops, right);
    }

    protected Ast parseExp5Ast(TokenIterator<Token> i) {
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
        /**/i.current().getValue().equals("keys") ||
        /**/i.current().getValue().equals("typeof") ||
        /**/i.current().getValue().equals("-")
        /**/)) {
            ops.add((String) i.current().getValue());
            i.next(); // chupa op
        }
        Ast right = parseExp6Ast(i);

        if (ops.isEmpty()) {
            return right;
        }
        return new UnaryAst(ops, right);
    }

    protected Ast parseExp6Ast(TokenIterator<Token> i) {
        Ast left = parseExp7Ast(i);
        List<FunctionCall> calls = new ArrayList<>();

        while (i.hasNext() && i.current().getType() == EToken.SYM && i.current().getValue().equals("->")) {
            i.next(); // chupa ->
            if (i.hasNext() && i.current().getType() == EToken.SYM) {
                String methodName = (String) i.current().getValue();
                i.next(); // chupa ident
                if (i.hasNext() && i.current().getType() == EToken.OPEN_PARENTESIS) {
                    i.next(); // chupa (
                    List<Ast> args = new ArrayList<>();
                    if (i.current().getType() == EToken.CLOSE_PARENTESIS) {
                        i.next(); // chupa )
                    } else {
                        args.add(parseExpression(i));
                        while (i.hasNext() && i.current().getType() == EToken.SYM
                                && i.current().getValue().equals(",")) {
                            i.next(); // chupa ,
                            args.add(parseExpression(i));
                        }
                        if (i.current().getType() != EToken.CLOSE_PARENTESIS) {
                            throw new RuntimeException("expected ) " + i.current());
                        }
                        i.next(); // chupa )
                    }
                    calls.add(new FunctionCall(methodName, args));
                }
            }
        }

        if (calls.isEmpty()) {
            return left;
        } else {
            return new FunctionCallAst(left, calls);
        }
    }

    protected Ast parseExp7Ast(TokenIterator<Token> i) {
        Ast left = parseExpNAst(i);
        List<Ast> right = new ArrayList<>();
        while (i.hasNext() && i.current().getType() == EToken.OPEN_CLAU) {
            i.next(); // chupa [

            right.add(parseExpression(i));

            if (i.current().getType() != EToken.CLOSE_CLAU) {
                throw new TokenException(i.current().getSourceRef(),
                        "expected " + EToken.OPEN_CLAU + ", but readed " + i.current());
            }
            i.next(); // chupa ]
        }

        if (right.isEmpty()) {
            return left;
        }
        return new Exp7Ast(left, right);
    }

    class FunctionCall {

        public final String methodName;
        public final List<Ast> args;

        public FunctionCall(String methodName, List<Ast> args) {
            super();
            this.methodName = methodName;
            this.args = args;
        }
    }

    static class FunctionCallAst implements Ast {

        final Ast left;
        final List<FunctionCall> calls;

        public FunctionCallAst(Ast left, List<FunctionCall> calls) {
            super();
            this.left = left;
            this.calls = calls;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object o = left.evaluate(model);
            for (FunctionCall call : calls) {
                List<Object> argValues = new ArrayList<>();
                for (Ast a : call.args) {
                    argValues.add(a.evaluate(model));
                }
                o = JRuntime.invokeFunc(o, call.methodName, argValues);
            }
            return o;
        }

        @Override
        public SourceRef getSourceRef() {
            return left.getSourceRef();
        }

    }

    protected Ast parseExpNAst(TokenIterator<Token> i) {
        EToken type = i.current().getType();
        switch (type) {
        case OPEN_PARENTESIS: {
            i.next();// chupa (
            Ast r = parseExpression(i);
            if (i.current().getType() != EToken.CLOSE_PARENTESIS) {
                throw new RuntimeException("expected )");
            }
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

    public static class TernaryAst implements Ast {

        final Ast ifAst;
        final Ast thenAst;
        final Ast elseAst;

        public TernaryAst(Ast ifAst, Ast thenAst, Ast elseAst) {
            super();
            this.ifAst = ifAst;
            this.thenAst = thenAst;
            this.elseAst = elseAst;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object ifValue = ifAst.evaluate(model);
            if (JRuntime.isTrue(ifValue)) {
                return thenAst.evaluate(model);
            } else {
                return elseAst.evaluate(model);
            }
        }

        @Override
        public SourceRef getSourceRef() {
            return ifAst.getSourceRef();
        }
    }

    public static class MultipleBinaryAst implements Ast {

        final Ast left;
        final List<String> ops;
        final List<Ast> right;

        public MultipleBinaryAst(Ast left, List<String> ops, List<Ast> right) {
            super();
            this.left = left;
            this.ops = ops;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object r = left.evaluate(model);
            for (int i = 0; i < ops.size(); i++) {
                String op = ops.get(i);
                try {
                    Object rightVal = right.get(i).evaluate(model);

                    if ("+".equals(op)) {
                        r = JRuntime.add(r, rightVal);
                    } else if ("-".equals(op)) {
                        r = JRuntime.sub(r, rightVal);
                    } else if ("*".equals(op)) {
                        r = JRuntime.mul(r, rightVal);
                    } else if ("/".equals(op)) {
                        r = JRuntime.div(r, rightVal);
                    } else if ("%".equals(op)) {
                        r = JRuntime.mod(r, rightVal);
                    } else if ("and".equals(op) || "&&".equals(op)) {
                        r = JRuntime.and(r, rightVal);
                    } else if ("or".equals(op) || "||".equals(op)) {
                        r = JRuntime.or(r, rightVal);
                    } else {
                        throw new RuntimeException(op);
                    }

                } catch (Exception e) {
                    throw new TokenException(right.get(i).getSourceRef(), "error operating: " + op, e);
                }
            }
            return r;
        }

        @Override
        public String toString() {
            if (ops.isEmpty()) {
                return String.valueOf(left);
            } else {
                StringBuilder s = new StringBuilder();
                s.append("(");
                s.append(left);
                for (int i = 0; i < ops.size(); i++) {
                    s.append(" " + ops.get(i) + " ");
                    s.append(right.get(i));
                }
                s.append(")");
                return s.toString();
            }
        }

        @Override
        public SourceRef getSourceRef() {
            return left.getSourceRef();
        }

    }

    public static class SingleBinaryAst implements Ast {

        final Ast left;
        final String op;
        final Ast right;

        public SingleBinaryAst(Ast left, String op, Ast right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object r = left.evaluate(model);
            if (op != null) {
                try {
                    Object rightVal = right.evaluate(model);
                    if ("eq".equals(op) || "==".equals(op)) {
                        r = JRuntime.eq(r, rightVal);
                    } else if ("ne".equals(op) || "!=".equals(op)) {
                        r = JRuntime.ne(r, rightVal);
                    } else if ("lt".equals(op) || "<".equals(op)) {
                        r = JRuntime.lt(r, rightVal);
                    } else if ("gt".equals(op) || ">".equals(op)) {
                        r = JRuntime.gt(r, rightVal);
                    } else if ("le".equals(op) || "<=".equals(op)) {
                        r = JRuntime.le(r, rightVal);
                    } else if ("ge".equals(op) || ">=".equals(op)) {
                        r = JRuntime.ge(r, rightVal);
                    } else {
                        throw new RuntimeException(op);
                    }
                } catch (Exception e) {
                    throw new TokenException(left.getSourceRef(), "error operating: " + op, e);
                }
            }
            return r;
        }

        @Override
        public String toString() {
            if (op == null) {
                return String.valueOf(left);
            } else {
                return "(" + left + " " + op + " " + right + ")";
            }
        }

        @Override
        public SourceRef getSourceRef() {
            return left.getSourceRef();
        }

    }

    public static class UnaryAst implements Ast {

        final List<String> ops;
        final Ast right;

        public UnaryAst(List<String> ops, Ast right) {
            super();
            this.ops = ops;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object r = right.evaluate(model);
            for (int i = ops.size() - 1; i >= 0; i--) {
                String op = ops.get(i);
                try {
                    if ("not".equals(op)) {
                        r = JRuntime.not(r);
                    } else if ("byte".equals(op)) {
                        r = JRuntime.toByte(r);
                    } else if ("short".equals(op)) {
                        r = JRuntime.toShort(r);
                    } else if ("int".equals(op)) {
                        r = JRuntime.toInteger(r);
                    } else if ("long".equals(op)) {
                        r = JRuntime.toLong(r);
                    } else if ("float".equals(op)) {
                        r = JRuntime.toFloat(r);
                    } else if ("double".equals(op)) {
                        r = JRuntime.toDouble(r);
                    } else if ("string".equals(op)) {
                        r = JRuntime.toString(r);
                    } else if ("keys".equals(op)) {
                        r = JRuntime.toKeysList(r);
                    } else if ("typeof".equals(op)) {
                        r = JRuntime.typeOf(r);
                    } else if ("-".equals(op)) {
                        r = JRuntime.neg(r);
                    } else {
                        throw new RuntimeException(op);
                    }
                } catch (Exception e) {
                    throw new TokenException(right.getSourceRef(), "error operating: " + op, e);
                }
            }
            return r;
        }

        @Override
        public String toString() {

            if (!ops.isEmpty()) {
                StringJoiner j = new StringJoiner(" ");
                for (String op : ops) {
                    j.add(op);
                }
                return "(" + j.toString() + " " + right + ")";
            }
            return right.toString();
        }

        @Override
        public SourceRef getSourceRef() {
            return right.getSourceRef();
        }

    }

    public static class Exp7Ast implements Ast {

        final Ast left;
        final List<Ast> right;

        public Exp7Ast(Ast left, List<Ast> right) {
            super();
            this.left = left;
            this.right = right;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            Object r = left.evaluate(model);
            for (Ast ri : right) {
                try {
                    Object key = ri.evaluate(model);
                    r = JRuntime.getByKey(r, key);
                } catch (Exception e) {
                    throw new TokenException(ri.getSourceRef(), "error operating: ", e);
                }
            }
            return r;
        }

        @Override
        public String toString() {
            if (right.isEmpty()) {
                return String.valueOf(left);
            } else {
                StringBuilder s = new StringBuilder();
                s.append("(");
                s.append(left);
                for (Ast i : right) {
                    s.append("[");
                    s.append(i);
                    s.append("]");
                }
                s.append(")");
                return s.toString();
            }
        }

        @Override
        public SourceRef getSourceRef() {
            return left.getSourceRef();
        }

    }

    public static class ExpNAst implements Ast {

        final SourceRef sourceRef;
        final Object value;

        public ExpNAst(SourceRef sourceRef, Object value) {
            super();
            this.sourceRef = sourceRef;
            this.value = value;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public SourceRef getSourceRef() {
            return sourceRef;
        }

    }

    public static class VarAst implements Ast {

        final SourceRef sourceRef;
        final List<String> idents;

        public VarAst(SourceRef sourceRef, List<String> idents) {
            super();
            this.sourceRef = sourceRef;
            this.idents = idents;
        }

        @Override
        public Object evaluate(Map<String, Object> model) {
            try {
                Object o = model;
                for (String i : idents) {
                    if (o == null) {
                        throw new TokenException(sourceRef, "nullpointer evaluating: " + i + " in " + idents);
                    }
                    o = JRuntime.getByKey(o, i);
                }
                return o;
            } catch (Exception e) {
                throw new TokenException(sourceRef, "evaluating: " + idents, e);
            }
        }

        @Override
        public String toString() {
            StringJoiner s = new StringJoiner(".");
            for (String i : idents) {
                s.add(i);
            }
            return s.toString();
        }

        @Override
        public SourceRef getSourceRef() {
            return sourceRef;
        }

    }

}
