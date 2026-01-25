package com.yanny.ali.compatibility.common;

@FunctionalInterface
public interface QuintConsumer<T, U, V, X, Y> {
    void accept(T t, U u, V v, X x, Y y);
}
