package com.yanny.alicompat.compat.sophisticatedstorage;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.server.IngredientTooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.p3pp3rf1y.sophisticatedstorage.crafting.BaseTierWoodenStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorage.data.CopyStorageDataFunction;
import org.jetbrains.annotations.NotNull;

public class SophisticatedStorageCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SophisticatedStorageLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopyStorageDataFunction.class, CopyStorageDataFunctionAccessor::new);

        registry.registerIngredientTooltip(BaseTierWoodenStorageIngredient.class, IngredientTooltipUtils::getIngredientTooltip);
    }
}
