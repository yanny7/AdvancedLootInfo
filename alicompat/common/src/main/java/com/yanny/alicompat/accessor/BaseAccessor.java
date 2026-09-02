package com.yanny.alicompat.accessor;

public abstract class BaseAccessor<T> {
    protected final T parent;

    public BaseAccessor(T parent) {
        this.parent = parent;
    }
}
