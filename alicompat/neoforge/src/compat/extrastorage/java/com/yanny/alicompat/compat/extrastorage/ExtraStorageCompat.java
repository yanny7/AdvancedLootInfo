package com.yanny.alicompat.compat.extrastorage;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import edivad.extrastorage.loottable.StorageBlockLootFunction;
import org.jetbrains.annotations.NotNull;

public class ExtraStorageCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ExtraStorageLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(StorageBlockLootFunction.class, ExtraStorageCompat::storageBlockTooltip);
    }

    @NotNull
    private static TooltipBuilder storageBlockTooltip(IServerUtils utils, StorageBlockLootFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ExtraStorageLang.Functions.COPY_STORAGE_ID);
    }
}
