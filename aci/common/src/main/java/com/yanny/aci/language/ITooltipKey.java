package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

public interface ITooltipKey extends IMultiKey {
    @Override
    @NotNull
    default String singular() {
        return getTranslation().singular();
    }

    @Override
    @NotNull
    default String plural() {
        return getTranslation().plural();
    }

    @NotNull
    Translation getTranslation();

    @NotNull
    default String englishSingular() {
        return getTranslation().sEng();
    }

    @NotNull
    default String englishPlural() {
        return getTranslation().pEng();
    }
}
