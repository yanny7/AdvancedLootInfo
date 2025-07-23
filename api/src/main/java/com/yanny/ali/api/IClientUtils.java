package com.yanny.ali.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IClientUtils extends ICommonUtils {
    List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth);

    <T extends IDataNode> IClientRegistry.NodeFactory<T> getNodeFactory(ResourceLocation id);

    List<ItemStack> getItems(ResourceKey<LootTable> location);
}
