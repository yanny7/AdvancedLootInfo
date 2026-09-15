package com.yanny.alicompat.compat.refinedstorage;

import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridLootItemFunction;
import com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockLootItemFunction;
import com.refinedmods.refinedstorage.common.support.energy.EnergyLootItemFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;

public class RefinedStorageCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return RefinedStorageLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(StorageBlockLootItemFunction.class, RefinedStorageCompat::storageBlockTooltip);
        registry.registerFunctionTooltip(PortableGridLootItemFunction.class, RefinedStorageCompat::portableGridTooltip);
        registry.registerFunctionTooltip(EnergyLootItemFunction.class, RefinedStorageCompat::energyTooltip);
    }

    @NotNull
    private static TooltipBuilder storageBlockTooltip(IServerUtils utils, StorageBlockLootItemFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RefinedStorageLang.Functions.COPY_STORAGE_ID);
    }

    @NotNull
    private static TooltipBuilder portableGridTooltip(IServerUtils utils, PortableGridLootItemFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RefinedStorageLang.Functions.COPY_PORTABLE_GRID_DATA);
    }

    @NotNull
    private static TooltipBuilder energyTooltip(IServerUtils utils, EnergyLootItemFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RefinedStorageLang.Functions.COPY_STORED_ENERGY);
    }
}
