package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IWidgetUtils extends IClientUtils {
    void addSlotWidget(Either<ItemStack, TagKey<Item>> item, IDataNode entry, RelativeRect rect);
}
