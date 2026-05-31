package com.yanny.aci.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface ICoreWidgetUtils<
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?, ?, ?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?>
        > extends ICoreClientUtils<TDataNode, TWidgetUtils, TClientUtils> {
    void addSlotWidget(Either<ItemStack, TagKey<? extends ItemLike>> item, TDataNode entry, RelativeRect rect);
}
