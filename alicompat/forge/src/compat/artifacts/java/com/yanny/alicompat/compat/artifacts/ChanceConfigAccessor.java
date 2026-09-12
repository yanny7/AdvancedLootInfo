package com.yanny.alicompat.compat.artifacts;

import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;

import java.util.function.Supplier;

@ClassAccessor("artifacts.loot.ConfigValueChance$ChanceConfig")
public class ChanceConfigAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private String name;
    @FieldAccessor
    private Supplier<Double> value;

    public ChanceConfigAccessor(Object parent) {
        super(parent);
    }

    public String getName() {
        return name;
    }

    public float getChance() {
        return value.get().floatValue();
    }
}
