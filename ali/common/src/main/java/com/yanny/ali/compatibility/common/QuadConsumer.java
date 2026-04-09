package com.yanny.ali.compatibility.common;

@FunctionalInterface
public interface QuadConsumer<T, U, V, X> {
    void accept(T t, U u, V v, X x);
}
