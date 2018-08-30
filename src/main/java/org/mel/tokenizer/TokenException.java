package org.mel.tokenizer;

public class TokenException extends RuntimeException {

    private static final long serialVersionUID = -4886197881544888461L;

    final SourceRef sourceRef;

    public TokenException(SourceRef sourceRef, String message) {
        super(sourceRef + ": " + message);
        this.sourceRef = sourceRef;
    }

    public TokenException(SourceRef sourceRef, String message, Throwable t) {
        super(sourceRef + ": " + message, t);
        this.sourceRef = sourceRef;
    }

}
