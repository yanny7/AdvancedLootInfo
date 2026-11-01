package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.network.syncher.EntityDataAccessor;

import java.lang.reflect.Method;
import java.util.List;

public sealed interface VariantHint {
    record Data(EntityDataAccessor<?> accessor, List<Object> values) implements VariantHint {
    }

    record Setter(Method method, List<Object> values) implements VariantHint {
    }
}
