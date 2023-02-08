package org.mel.tokenizer;

public class Token {

    final EToken type;
    final Object value;

    final SourceRef sourceRef;

    public Token(EToken type, Object value, SourceRef sourceRef) {
        super();
        this.type = type;
        this.value = value;
        this.sourceRef = sourceRef;
    }

    public EToken getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public SourceRef getSourceRef() {
        return this.sourceRef;
    }

    @Override
    public String toString() {
        return type.name() + ":" + getValue();
    }
}