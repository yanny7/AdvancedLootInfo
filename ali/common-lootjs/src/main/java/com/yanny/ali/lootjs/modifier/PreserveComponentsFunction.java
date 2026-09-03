package com.yanny.ali.lootjs.modifier;

import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class PreserveComponentsFunction extends BaseLootItemFunction {
    private final List<DataComponentType<?>> types;

    public PreserveComponentsFunction(DataComponentType<?>[] types) {
        this.types = List.of(types);
    }

    public List<DataComponentType<?>> getTypes() {
        return types;
    }
}
