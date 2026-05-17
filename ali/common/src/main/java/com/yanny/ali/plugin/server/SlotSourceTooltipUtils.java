package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.item.slot.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSlotListTooltip;

public class SlotSourceTooltipUtils {
    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils ignoredUtils, EmptySlotSource ignoredSlot) {
        return TooltipBuilder.keyOnly(Lang.SlotSource.EMPTY);
    }

    @NotNull
    public static TooltipBuilder getContentsTooltip(IServerUtils utils, ContentsSlotSource slot) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot.component).build(Lang.Value.COMPONENT));
            b.add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build(Lang.Branch.SLOT_SOURCE));
        }).key(Lang.SlotSource.CONTENTS);
    }

    @NotNull
    public static TooltipBuilder getSlotRangeTooltip(IServerUtils utils, RangeSlotSource slot) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot.source).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, slot.slotRange).build(Lang.Value.SLOT_RANGE));
        }).key(Lang.SlotSource.SLOT_RANGE);
    }

    @NotNull
    public static TooltipBuilder getLimitSlotsTooltip(IServerUtils utils, LimitSlotSource slot) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot.limit).build(Lang.Value.LIMIT));
            b.add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build(Lang.Branch.SLOT_SOURCE));
        }).key(Lang.SlotSource.LIMIT_SLOTS);
    }

    @NotNull
    public static TooltipBuilder getFilteredTooltip(IServerUtils utils, FilteredSlotSource slot) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, slot.filter).build(Lang.Branch.FILTER));
            b.add(TooltipBuilder.array((c) -> c.add(utils.getSlotSourceTooltip(utils, slot.slotSource))).build(Lang.Branch.SLOT_SOURCE));
        }).key(Lang.SlotSource.FILTERED);
    }

    @NotNull
    public static TooltipBuilder getGroupTooltip(IServerUtils utils, GroupSlotSource slot) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.array((c) -> c.add(getSlotListTooltip(utils, slot.terms))).build(Lang.Branch.SLOTS)))
                .key(Lang.SlotSource.GROUP);
    }
}
