package com.yanny.alicompat.compat.refinedstorage;

import com.refinedmods.refinedstorage.loottable.ControllerLootFunction;
import com.refinedmods.refinedstorage.loottable.CrafterLootFunction;
import com.refinedmods.refinedstorage.loottable.PortableGridBlockLootFunction;
import com.refinedmods.refinedstorage.loottable.StorageBlockLootFunction;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class RefinedStorageCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return RefinedStorageLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, ControllerLootFunction.class, ControllerLootFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, CrafterLootFunction.class, CrafterLootFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, PortableGridBlockLootFunction.class, PortableGridBlockLootFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, StorageBlockLootFunction.class, StorageBlockLootFunctionAccessor::new);
    }
}
