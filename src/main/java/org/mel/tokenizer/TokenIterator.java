package org.mel.tokenizer;

import java.util.List;

public class TokenIterator<T> {

    final List<T> list;
    int pos;

    public TokenIterator(List<T> list) {
        super();
        this.list = list;
        this.pos = 0;
    }

    public T current() {
        return list.get(pos);
    }

    public boolean notEof() {
        return pos < list.size();
    }

    public void next() {
        pos++;
    }

    public boolean hasNext() {
        return pos < list.size() - 1;
    }

}
