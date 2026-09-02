package com.yanny.alicompat;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ICompatTranslations {
    @NotNull
    String targetModId();

    @NotNull
    Map<String, String> getTranslations();
}
