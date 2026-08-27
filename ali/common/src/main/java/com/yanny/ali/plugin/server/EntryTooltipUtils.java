package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.level.storage.loot.entries.*;
import org.jetbrains.annotations.NotNull;

public class EntryTooltipUtils {
    @NotNull
    public static TooltipBuilder getItemTooltip(IServerUtils utils, LootItem entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entry.item).build(Lang.Value.ITEM));
            addSingleton(b, utils, entry);
        }, Lang.Entry.ITEM);
    }

    @NotNull
    public static TooltipBuilder getTagTooltip(IServerUtils utils, TagEntry entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entry.tag).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, entry.expand).build(Lang.Value.EXPAND));
            addSingleton(b, utils, entry);
        }, Lang.Entry.TAG);
    }

    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils utils, EmptyLootItem entry) {
        return TooltipBuilder.array((b) -> addSingleton(b, utils, entry), Lang.Entry.EMPTY);
    }

    @NotNull
    public static TooltipBuilder getDynamicTooltip(IServerUtils utils, DynamicLoot entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entry.name).build(Lang.Value.NAME));
            addSingleton(b, utils, entry);
        }, Lang.Entry.DYNAMIC);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, LootTableReference entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entry.name).build(Lang.Value.LOOT_TABLE));
            addSingleton(b, utils, entry);
        }, Lang.Entry.LOOT_TABLE);
    }

    @NotNull
    public static TooltipBuilder getAlternativesTooltip(IServerUtils utils, AlternativesEntry entry) {
        return TooltipBuilder.array((b) -> addComposite(b, utils, entry), Lang.Entry.ALTERNATIVES);
    }

    @NotNull
    public static TooltipBuilder getGroupTooltip(IServerUtils utils, EntryGroup entry) {
        return TooltipBuilder.array((b) -> addComposite(b, utils, entry), Lang.Entry.GROUP);
    }

    @NotNull
    public static TooltipBuilder getSequentialTooltip(IServerUtils utils, SequentialEntry entry) {
        return TooltipBuilder.array((b) -> addComposite(b, utils, entry), Lang.Entry.SEQUENCE);
    }

    private static void addSingleton(TooltipBuilder builder, IServerUtils utils, LootPoolSingletonContainer entry) {
        builder.add(TooltipUtils.getWeightTooltip(entry.weight));
        builder.add(TooltipUtils.getQualityTooltip(entry.quality));
        builder.add(utils.getValueTooltip(utils, entry.conditions).build(Lang.Branch.PREDICATES));
        builder.add(utils.getValueTooltip(utils, entry.functions).build(Lang.Branch.MODIFIERS));
    }

    private static void addComposite(TooltipBuilder builder, IServerUtils utils, CompositeEntryBase entry) {
        builder.add(TooltipBuilder.array((b) -> {
            for (LootPoolEntryContainer child : entry.children) {
                b.add(utils.getEntryTooltip(utils, child));
            }
        }, Lang.Branch.ENTRIES));
        builder.add(utils.getValueTooltip(utils, entry.conditions).build(Lang.Branch.PREDICATES));
    }
}
