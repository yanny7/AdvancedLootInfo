package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.util.ResourceOrTag;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

public class ResourceOrTagAccessor extends BaseAccessor<ResourceOrTag<?>> implements IValueTooltip {
    @FieldAccessor
    private Either<ResourceKey<?>, TagKey<?>> either;

    public ResourceOrTagAccessor(ResourceOrTag<?> parent) {
        super(parent);
    }

    @NotNull
    public static ResourceOrTagAccessor of(ResourceOrTag<?> resourceOrTag) {
        return ReflectionUtils.copyClassData(ResourceOrTagAccessor.class, resourceOrTag, ResourceOrTag.class);
    }

    @NotNull
    public ResourceLocation getRegistry() {
        return either.map(ResourceKey::registry, (tag) -> tag.registry().location());
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return either.map((key) -> utils.getValueTooltip(utils, key), (tag) -> utils.getValueTooltip(utils, tag));
    }
}
