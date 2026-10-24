package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
interface MethodModel {
    Value NO_OPINION = new Value.Known(new Object());

    @Nullable
    Value apply(CallContext context, Call call);
}
