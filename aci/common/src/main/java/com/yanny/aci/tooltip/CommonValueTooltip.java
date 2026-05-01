package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class CommonValueTooltip<
        TTooltipNode    extends ICoreTooltipNode<TServerUtils>,
        TKeyTooltipNode extends ICoreKeyTooltipNode<TTooltipNode, TKeyTooltipNode>,
        TServerUtils    extends ICoreServerUtils<TKeyTooltipNode, TTooltipNode, TServerUtils>,
        TServerRegistry extends ICoreServerRegistry<TServerUtils, TKeyTooltipNode>
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
        registry.registerValueTooltip(IntList.class, this::getIntListTooltip);
        registry.registerValueTooltip(EitherHolder.class, this::getEitherHolderTooltip);
        registry.registerValueTooltip(Property.class, this::getPropertyTooltip);
    }

    private TKeyTooltipNode getCollectionTooltip(TServerUtils utils, Collection<?> collection) {
        if (collection.isEmpty()) {
            return utils.getEmptyNode();
        }

        TKeyTooltipNode tooltip = utils.getBranchNode();

        for (Object o : collection) {
            tooltip.add(utils.buildTooltip(utils.getValueTooltip(utils, o)));
        }

        return tooltip;
    }

    @NotNull
    private TKeyTooltipNode getHolderTooltip(TServerUtils utils, Holder<?> holder) {
        return utils.getValueTooltip(utils, holder.value());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    private TKeyTooltipNode getOptionalTooltip(TServerUtils utils, Optional<?> optional) {
        return optional.map((v) -> utils.getValueTooltip(utils, v)).orElse(utils.getEmptyNode());
    }

    @NotNull
    private TKeyTooltipNode getBooleanTooltip(TServerUtils utils, Boolean value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getIntegerTooltip(TServerUtils utils, int value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getLongTooltip(TServerUtils utils, Long value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getByteTooltip(TServerUtils utils, Byte value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getStringTooltip(TServerUtils utils, String value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getFloatTooltip(TServerUtils utils, Float value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getDoubleTooltip(TServerUtils utils, Double value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getEnumTooltip(TServerUtils utils, Enum<?> value) {
        return utils.getValueNode(value.name());
    }

    @NotNull
    private TKeyTooltipNode getResourceLocationTooltip(TServerUtils utils, ResourceLocation value) {
        return utils.getValueNode(value);
    }

    @NotNull
    private TKeyTooltipNode getResourceKeyTooltip(TServerUtils utils, ResourceKey<?> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    private TKeyTooltipNode getTagKeyTooltip(TServerUtils utils, TagKey<?> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    private TKeyTooltipNode getComponentTooltip(TServerUtils utils, Component component) {
        return utils.getComponentNode(component.copy());
    }

    @NotNull
    private TKeyTooltipNode getUUIDTooltip(TServerUtils utils, UUID uuid) {
        return utils.getValueNode(uuid);
    }

    @NotNull
    private TKeyTooltipNode getCompoundTagTooltip(TServerUtils utils, CompoundTag tag) {
        return utils.getValueTooltip(utils, tag.toString());
    }

    @NotNull
    public TKeyTooltipNode getIntListTooltip(TServerUtils utils, IntList data) {
        return utils.getValueTooltip(utils, data.toString());
    }

    @NotNull
    public TKeyTooltipNode getEitherHolderTooltip(TServerUtils utils, EitherHolder<?> holder) {
        return holder.asEither().map((v) -> utils.getValueTooltip(utils, v.value()), (k) -> utils.getValueTooltip(utils, k));
    }

    @NotNull
    private TKeyTooltipNode getPropertyTooltip(TServerUtils utils, Property<?> property) {
        return utils.getValueTooltip(utils, property.getName());
    }
}
