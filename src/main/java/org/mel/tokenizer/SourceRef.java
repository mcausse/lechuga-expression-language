package org.mel.tokenizer;

public class SourceRef {

    final String sourceFile;
    final int line;
    final int column;

    public SourceRef(String sourceFile, int line, int column) {
        super();
        this.sourceFile = sourceFile;
        this.line = line;
        this.column = column;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("[at %s:%s,%s]", sourceFile, line, column);
    }

}
