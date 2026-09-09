package com.yanny.alicompat.compat.extrastorage;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import edivad.extrastorage.loottable.AdvancedCrafterLootFunction;
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
        PluginUtils.registerFunctionTooltip(registry, StorageBlockLootFunction.class, StorageBlockLootFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, AdvancedCrafterLootFunction.class, AdvancedCrafterLootFunctionAccessor::new);
    }
}
