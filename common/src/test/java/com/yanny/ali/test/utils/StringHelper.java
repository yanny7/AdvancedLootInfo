package com.yanny.ali.test.utils;

import org.jetbrains.annotations.NotNull;

public class StringHelper {
    private final StringBuilder sb = new StringBuilder();
    private final StringBuilder pre = new StringBuilder();
    private final StringBuilder post = new StringBuilder();
    private final StringBuilder tmp = new StringBuilder();

    @NotNull
    public static StringHelper str() {
        return new StringHelper();
    }

    public StringHelper txtBold(String s) {
        this.txt(s);
        pre.append("*");
        post.append("*");
        return this;
    }

    public StringHelper txt(String s) {
        addAndReset();

        pre.append("[");
        post.append("]");
        tmp.append(s);
        return this;
    }

    public StringHelper paramBold(String s) {
        this.param(s);
        pre.append("*");
        post.append("*");
        return this;
    }

    public StringHelper param(String s) {
        addAndReset();

        pre.append("<");
        post.append(">");
        tmp.append(s);
        return this;
    }

    public String build() {
        addAndReset();
        return sb.toString();
    }

    private void addAndReset() {
        if (!tmp.isEmpty()) {
            sb.append(pre).append(tmp).append(post.reverse());
            pre.setLength(0);
            post.setLength(0);
            tmp.setLength(0);
        }
    }
}
