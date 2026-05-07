package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

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
        registry.registerValueTooltip(Property.class, this::getPropertyTooltip);
    }

    private TooltipBuilder getCollectionTooltip(TServerUtils utils, Collection<?> collection) {
        if (collection.isEmpty()) {
            return TooltipBuilder.empty();
        }

        return TooltipBuilder.array((b) -> {
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
        return TooltipBuilder.value(value);
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
        return TooltipBuilder.component(component);
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
    private TooltipBuilder getPropertyTooltip(TServerUtils utils, Property<?> property) {
        return utils.getValueTooltip(utils, property.getName());
    }
}
