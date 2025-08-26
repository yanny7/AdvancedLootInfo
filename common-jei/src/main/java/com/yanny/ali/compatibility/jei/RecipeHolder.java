package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.IWidget;
import com.yanny.ali.compatibility.common.IType;

import java.util.List;

public final class RecipeHolder<T extends IType> {
    private final T type;
    private IWidget widget;
    private List<JeiBaseLoot.Holder> holders;

    public RecipeHolder(T type) {
        this.type = type;
        widget = null;
        holders = null;
    }

    public RecipeHolder(T type, IWidget widget, List<JeiBaseLoot.Holder> holders) {
        this.type = type;
        this.widget = widget;
        this.holders = holders;
    }

    public T type() {
        return type;
    }

    public IWidget getWidget() {
        return widget;
    }

    public void setWidget(IWidget widget) {
        this.widget = widget;
    }

    public List<JeiBaseLoot.Holder> getHolders() {
        return holders;
    }

    public void setHolders(List<JeiBaseLoot.Holder> holders) {
        this.holders = holders;
    }
}
