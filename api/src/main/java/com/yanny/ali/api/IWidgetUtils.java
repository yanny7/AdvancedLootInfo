package com.yanny.ali.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IWidgetUtils extends IClientUtils {
    Rect addSlotWidget(ItemStack item, IDataNode entry, int x, int y);

    Rect addSlotWidget(TagKey<Item> item, IDataNode entry, int x, int y);

}
