package com.yanny.awi.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.ICoreWidgetUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface IWidgetUtils extends ICoreWidgetUtils<Either<Block, TagKey<Block>>, IDataNode, IWidgetUtils, IClientUtils> {
}
