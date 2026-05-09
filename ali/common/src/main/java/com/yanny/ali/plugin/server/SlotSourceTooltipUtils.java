package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.slot.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSlotListTooltip;

public class SlotSourceTooltipUtils {
    @NotNull
    public static TooltipNode getEmptyTooltip(IServerUtils ignoredUtils, EmptySlotSource ignoredSlot) {
        return TooltipBuilder.keyOnly("ali.type.slot_source.empty").build();
    }

    @NotNull
    public static TooltipNode getContentsTooltip(IServerUtils utils, ContentsSlotSource slot) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, slot.component).build("ali.property.value.component"))
                .add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build("ali.property.branch.slot_source"))
                )
                .build("ali.type.slot_source.contents");
    }

    @NotNull
    public static TooltipNode getSlotRangeTooltip(IServerUtils utils, RangeSlotSource slot) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, slot.source).build("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, slot.slotRange).build("ali.property.value.slot_range"))
                )
                .build("ali.type.slot_source.slot_range");
    }

    @NotNull
    public static TooltipNode getLimitSlotsTooltip(IServerUtils utils, LimitSlotSource slot) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, slot.limit).build("ali.property.value.limit"))
                .add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build("ali.property.branch.slot_source"))
                )
                .build("ali.type.slot_source.limit_slots");
    }

    @NotNull
    public static TooltipNode getFilteredTooltip(IServerUtils utils, FilteredSlotSource slot) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, slot.filter).build("ali.property.branch.filter"))
                .add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build("ali.property.branch.slot_source"))
                )
                .build("ali.type.slot_source.filtered");
    }

    @NotNull
    public static TooltipNode getGroupTooltip(IServerUtils utils, GroupSlotSource slot) {
        return TooltipBuilder.array((b) -> b
                .add(TooltipBuilder.array((c) -> c.add(getSlotListTooltip(utils, slot.terms))).build("ali.property.branch.slots"))
                )
                .build("ali.type.slot_source.group");
    }
}
