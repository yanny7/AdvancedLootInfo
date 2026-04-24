package com.yanny.aci.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface ICommonWidgetUtils<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>, DN extends ICommonDataNode<SU, TN>> {
    void addSlotWidget(Either<ItemStack, TagKey<? extends ItemLike>> item, DN entry, RelativeRect rect);
}
