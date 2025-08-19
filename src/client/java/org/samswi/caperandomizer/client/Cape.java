package org.samswi.caperandomizer.client;

import java.io.Closeable;

public class Cape implements Cloneable {
    public String id;
    public String url;
    public String name;

    public Cape(String id, String url, String name) {
        this.id = id;
        this.url = url;
        this.name = name;
    }

    @Override
    public Cape clone() {
        try {
            return (Cape) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
