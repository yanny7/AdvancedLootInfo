package com.yanny.aci;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonLogUtils {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @NotNull
    public static Logger getLogger(@NotNull String modId) {
        return new PrefixLogger(LoggerFactory.getLogger(STACK_WALKER.getCallerClass()), "[" + modId + "] ");
    }

    private CommonLogUtils() {}
}
