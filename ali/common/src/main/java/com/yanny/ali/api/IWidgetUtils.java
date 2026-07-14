package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.ICoreWidgetUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IWidgetUtils extends ICoreWidgetUtils<Either<ItemStack, TagKey<? extends ItemLike>>, IDataNode, IWidgetUtils, IClientUtils> {
}
