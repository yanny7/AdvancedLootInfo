package com.yanny.ali.compatibility.jei;

import com.yanny.ali.compatibility.common.IType;

import java.util.List;

public final class RecipeHolder<T extends IType> {
    private final T type;
    private JeiWidgetWrapper widgetWrapper;
    private List<JeiBaseLoot.Holder> holders;

    public RecipeHolder(T type) {
        this.type = type;
        widgetWrapper = null;
        holders = null;
    }

    public RecipeHolder(T type, JeiWidgetWrapper widgetWrapper, List<JeiBaseLoot.Holder> holders) {
        this.type = type;
        this.widgetWrapper = widgetWrapper;
        this.holders = holders;
    }

    public T type() {
        return type;
    }

    public JeiWidgetWrapper getWidgetWrapper() {
        return widgetWrapper;
    }

    public void setWidgetWrapper(JeiWidgetWrapper widgetWrapper) {
        this.widgetWrapper = widgetWrapper;
    }

    public List<JeiBaseLoot.Holder> getHolders() {
        return holders;
    }

    public void setHolders(List<JeiBaseLoot.Holder> holders) {
        this.holders = holders;
    }
}
