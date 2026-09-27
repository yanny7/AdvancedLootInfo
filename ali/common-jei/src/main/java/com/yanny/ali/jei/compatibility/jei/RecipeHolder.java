package com.yanny.ali.jei.compatibility.jei;

import com.yanny.ali.compatibility.common.IType;

public final class RecipeHolder<T extends IType> {
    private final T type;

    public RecipeHolder(T type) {
        this.type = type;
    }

    public T type() {
        return type;
    }
}
