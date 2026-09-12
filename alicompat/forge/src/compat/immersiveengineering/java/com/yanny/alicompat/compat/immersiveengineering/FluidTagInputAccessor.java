package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidTagInputAccessor extends BaseAccessor<FluidTagInput> implements IValueTooltip {
    @FieldAccessor
    private Either<TagKey<Fluid>, List<ResourceLocation>> fluidTag;

    @FieldAccessor
    private int amount;

    @FieldAccessor
    private CompoundTag nbtTag;

    public FluidTagInputAccessor(FluidTagInput parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fluidTag.left()).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, fluidTag.right()).build(Lang.Value.FLUID));
            b.add(utils.getValueTooltip(utils, amount).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, nbtTag).build(Lang.Value.NBT));
        });
    }
}
