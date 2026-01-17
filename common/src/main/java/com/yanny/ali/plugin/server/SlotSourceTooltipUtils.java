package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import net.minecraft.world.item.slot.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSlotListTooltip;

public class SlotSourceTooltipUtils {
    @NotNull
    public static ITooltipNode getEmptyTooltip(IServerUtils ignoredUtils, EmptySlotSource ignoredSlot) {
        return LiteralTooltipNode.translatable("ali.type.slot_source.empty");
    }

    @NotNull
    public static ITooltipNode getContentsTooltip(IServerUtils utils, ContentsSlotSource slot) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, slot.component).build("ali.property.value.component"))
                .add(BranchTooltipNode.branch().add(utils.getSlotSourceTooltip(utils, slot.slotSource)).build("ali.property.branch.slot_source"))
                .build("ali.type.slot_source.contents");
    }

    @NotNull
    public static ITooltipNode getSlotRangeTooltip(IServerUtils utils, RangeSlotSource slot) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, slot.source).build("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, slot.slotRange).build("ali.property.value.slot_range"))
                .build("ali.type.slot_source.slot_range");
    }

    @NotNull
    public static ITooltipNode getLimitSlotsTooltip(IServerUtils utils, LimitSlotSource slot) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, slot.limit).build("ali.property.value.limit"))
                .add(BranchTooltipNode.branch().add(utils.getSlotSourceTooltip(utils, slot.slotSource)).build("ali.property.branch.slot_source"))
                .build("ali.type.slot_source.limit_slots");
    }

    @NotNull
    public static ITooltipNode getFilteredTooltip(IServerUtils utils, FilteredSlotSource slot) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, slot.filter).build("ali.property.branch.filter"))
                .add(BranchTooltipNode.branch().add(utils.getSlotSourceTooltip(utils, slot.slotSource)).build("ali.property.branch.slot_source"))
                .build("ali.type.slot_source.filtered");
    }

    @NotNull
    public static ITooltipNode getGroupTooltip(IServerUtils utils, GroupSlotSource slot) {
        return BranchTooltipNode.branch()
                .add(BranchTooltipNode.branch().add(getSlotListTooltip(utils, slot.terms)).build("ali.property.branch.slots"))
                .build("ali.type.slot_source.group");
    }
}
