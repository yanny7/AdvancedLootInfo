package com.yanny.alicompat.compat.sophisticatedstorage;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.crafting.BaseTierWoodenStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorage.data.CopyStorageDataFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SophisticatedStorageCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SophisticatedStorageLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(CopyStorageDataFunction.class, SophisticatedStorageCompat::copyStorageDataTooltip);

        registry.registerValueTooltip(BaseTierWoodenStorageIngredient.class, SophisticatedStorageCompat::baseTierWoodenStorageTooltip);
    }

    @NotNull
    private static TooltipBuilder copyStorageDataTooltip(IServerUtils utils, CopyStorageDataFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, SophisticatedStorageLang.Functions.COPY_STORAGE_DATA);
    }

    @NotNull
    private static TooltipBuilder baseTierWoodenStorageTooltip(IServerUtils utils, BaseTierWoodenStorageIngredient ingredient) {
        List<ItemStack> items = ingredient.getItems().toList();

        return TooltipBuilder.array((b) -> items.forEach((i) -> b.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, i), items.size()))));
    }
}
