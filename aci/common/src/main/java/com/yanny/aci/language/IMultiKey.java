package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

public interface IMultiKey {
    @NotNull
    String singular();

    @NotNull
    String plural();
}
