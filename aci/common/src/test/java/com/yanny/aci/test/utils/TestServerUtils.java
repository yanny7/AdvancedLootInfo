package com.yanny.aci.test.utils;

import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestServerUtils implements ICoreServerUtils<TestServerUtils> {
    private final TooltipNodePalette palette;
    private final List<String> dictionary;

    public TestServerUtils(TooltipNodePalette palette, List<String> dictionary) {
        this.palette = palette;
        this.dictionary = dictionary;
    }

    @NotNull
    @Override
    public <T> TooltipBuilder getValueTooltip(TestServerUtils utils, @Nullable T value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ServerLevel getServerLevel() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return palette;
    }

    @NotNull
    @Override
    public HolderLookup.Provider lookupProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTranslationKeyIndex(String key) {
        return dictionary.indexOf(key);
    }
}
