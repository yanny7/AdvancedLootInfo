package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IClientUtils extends ICommonUtils {
    List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth);

    <T extends IDataNode> IClientRegistry.DataFactory<T> getDataNodeFactory(ResourceLocation id);

    <T extends ITooltipNode> IClientRegistry.TooltipFactory<T> getTooltipNodeFactory(ResourceLocation id);

    List<ItemStack> getLootItems(ResourceLocation location);

    List<Item> getTradeInputItems(ResourceLocation location);

    List<Item> getTradeOutputItems(ResourceLocation location);
}
