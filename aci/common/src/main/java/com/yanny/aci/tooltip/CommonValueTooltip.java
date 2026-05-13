package com.yanny.aci.tooltip;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.ICoreServerUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommonValueTooltip<
        TServerUtils    extends ICoreServerUtils<TServerUtils>,
        TServerRegistry extends ICoreServerRegistry<TServerUtils>
        > {
    public void registerAll(TServerRegistry registry) {
        registry.registerValueTooltip(Collection.class, this::getCollectionTooltip);
        registry.registerValueTooltip(Optional.class, this::getOptionalTooltip);
        registry.registerValueTooltip(Holder.class, this::getHolderTooltip);
        registry.registerValueTooltip(Boolean.class, this::getBooleanTooltip);
        registry.registerValueTooltip(Integer.class, this::getIntegerTooltip);
        registry.registerValueTooltip(Long.class, this::getLongTooltip);
        registry.registerValueTooltip(Short.class, this::getShortTooltip);
        registry.registerValueTooltip(Byte.class, this::getByteTooltip);
        registry.registerValueTooltip(String.class, this::getStringTooltip);
        registry.registerValueTooltip(Float.class, this::getFloatTooltip);
        registry.registerValueTooltip(Double.class, this::getDoubleTooltip);
        registry.registerValueTooltip(Enum.class, this::getEnumTooltip);
        registry.registerValueTooltip(ResourceLocation.class, this::getResourceLocationTooltip);
        registry.registerValueTooltip(ResourceKey.class, this::getResourceKeyTooltip);
        registry.registerValueTooltip(TagKey.class, this::getTagKeyTooltip);
        registry.registerValueTooltip(Component.class, this::getComponentTooltip);
        registry.registerValueTooltip(UUID.class, this::getUUIDTooltip);
        registry.registerValueTooltip(CompoundTag.class, this::getCompoundTagTooltip);
        registry.registerValueTooltip(IntList.class, this::getIntListTooltip);
        registry.registerValueTooltip(EitherHolder.class, this::getEitherHolderTooltip);
        registry.registerValueTooltip(Property.class, this::getPropertyTooltip);
        registry.registerValueTooltip(HolderSet.class, this::getHolderSetTooltip);
    }

    private TooltipBuilder getCollectionTooltip(TServerUtils utils, Collection<?> collection) {
        if (collection.isEmpty()) {
            return TooltipBuilder.empty();
        }

        return TooltipBuilder.branch((b) -> {
            for (Object o : collection) {
                b.add(utils.getValueTooltip(utils, o));
            }
        });
    }

    @NotNull
    private TooltipBuilder getHolderTooltip(TServerUtils utils, Holder<?> holder) {
        return utils.getValueTooltip(utils, holder.value());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    private TooltipBuilder getOptionalTooltip(TServerUtils utils, Optional<?> optional) {
        return optional.map((v) -> utils.getValueTooltip(utils, v)).orElse(TooltipBuilder.empty());
    }

    @NotNull
    private TooltipBuilder getBooleanTooltip(TServerUtils utils, Boolean value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getIntegerTooltip(TServerUtils utils, int value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getLongTooltip(TServerUtils utils, Long value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getShortTooltip(TServerUtils utils, Short value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getByteTooltip(TServerUtils utils, Byte value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getStringTooltip(TServerUtils utils, String value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getFloatTooltip(TServerUtils utils, Float value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getDoubleTooltip(TServerUtils utils, Double value) {
        return TooltipBuilder.value((float) (double) value);
    }

    @NotNull
    private TooltipBuilder getEnumTooltip(TServerUtils utils, Enum<?> value) {
        return TooltipBuilder.value(value.name());
    }

    @NotNull
    private TooltipBuilder getResourceLocationTooltip(TServerUtils utils, ResourceLocation value) {
        return TooltipBuilder.value(value);
    }

    @NotNull
    private TooltipBuilder getResourceKeyTooltip(TServerUtils utils, ResourceKey<?> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    private TooltipBuilder getTagKeyTooltip(TServerUtils utils, TagKey<?> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    private TooltipBuilder getComponentTooltip(TServerUtils utils, Component component) {
        return TooltipBuilder.component(Objects.requireNonNull(utils.lookupProvider()), component);
    }

    @NotNull
    private TooltipBuilder getUUIDTooltip(TServerUtils utils, UUID uuid) {
        return TooltipBuilder.value(uuid);
    }

    @NotNull
    private TooltipBuilder getCompoundTagTooltip(TServerUtils utils, CompoundTag tag) {
        return utils.getValueTooltip(utils, tag.toString());
    }

    @NotNull
    public TooltipBuilder getIntListTooltip(TServerUtils utils, IntList data) {
        return utils.getValueTooltip(utils, data.toString());
    }

    @NotNull
    public TooltipBuilder getEitherHolderTooltip(TServerUtils utils, EitherHolder<?> holder) {
        return holder.asEither().map((v) -> utils.getValueTooltip(utils, v.value()), (k) -> utils.getValueTooltip(utils, k));
    }

    @NotNull
    private TooltipBuilder getPropertyTooltip(TServerUtils utils, Property<?> property) {
        return utils.getValueTooltip(utils, property.getName());
    }

    public TooltipBuilder getHolderSetTooltip(TServerUtils utils, HolderSet<?> holderSet) {
        //noinspection unchecked
        Either<TagKey<?>, List<Holder<?>>> either = (Either<TagKey<?>, List<Holder<?>>>)(Object) holderSet.unwrap();
        Optional<TagKey<?>> left = either.left();
        Optional<List<Holder<?>>> right = either.right();

        if (left.isEmpty() && right.orElse(List.of()).isEmpty()) {
            return TooltipBuilder.empty();
        }

        return TooltipBuilder.array((b) -> {
            left.ifPresent((tagKey) -> b.add(utils.getValueTooltip(utils, tagKey).build("ali.property.value.tag")));
            right.ifPresent((list) -> {
                if (!list.isEmpty()) {
                    list.forEach((holder) -> b.add(utils.getValueTooltip(utils, holder)));
                }
            });
        });
    }
}
