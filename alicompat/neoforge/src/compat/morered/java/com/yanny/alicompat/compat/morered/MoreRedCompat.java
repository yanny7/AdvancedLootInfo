package com.yanny.alicompat.compat.morered;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IModCompat;
import net.commoble.morered.MoreRed;
import net.commoble.morered.mechanisms.GearsLootEntry;
import net.commoble.morered.wires.WireCountLootFunction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoreRedCompat implements IModCompat {
    private static final NumberExpr WIRE_COUNT = NumberExpr.range(1, 6);
    private static final NumberExpr GEAR_COUNT = NumberExpr.range(1, 6);

    @NotNull
    @Override
    public String targetModId() {
        return MoreRedLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(WireCountLootFunction.class, MoreRedCompat::wireCountTooltip);
        registry.registerCountModifier(WireCountLootFunction.class, MoreRedCompat::applyWireCount);

        registry.registerEntry(GearsLootEntry.class, MoreRedCompat::gearsNode);
        registry.registerEntryTooltip(GearsLootEntry.class, MoreRedCompat::gearsTooltip);
    }

    @NotNull
    private static TooltipBuilder wireCountTooltip(IServerUtils ignoredUtils, WireCountLootFunction ignoredFunction) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, MoreRedLang.Functions.SET_WIRE_COUNT);
    }

    private static NumberExpr applyWireCount(IServerUtils ignoredUtils, WireCountLootFunction ignoredFunction, NumberExpr count) {
        return WIRE_COUNT;
    }

    @NotNull
    private static IDataNode gearsNode(IServerUtils utils, GearsLootEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(entry, conditions);
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(entry, functions);
        float chance = NodeUtils.getChance(entry, rawChance, sumWeight);
        Either<ItemStack, TagKey<? extends ItemLike>> item = Either.right(MoreRed.Tags.Items.GEARS);
        LootCount count = NodeUtils.getCount(utils, GEAR_COUNT, allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, entry.quality, NodeUtils.getChance(utils, allConditions, chance), count, NodeUtils.getCountLimit(item), allFunctions, allConditions).build();

        return new ItemNode(chance, count.value(), item, tooltip, allFunctions, allConditions);
    }

    @NotNull
    private static TooltipBuilder gearsTooltip(IServerUtils utils, GearsLootEntry entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, MoreRed.Tags.Items.GEARS).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, GEAR_COUNT).build(Lang.Value.COUNT));
            b.add(TooltipUtils.getWeightTooltip(entry.weight));
            b.add(TooltipUtils.getQualityTooltip(entry.quality));
            b.add(utils.getValueTooltip(utils, entry.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, entry.functions).build(Lang.Branch.MODIFIERS));
        }, MoreRedLang.Entry.GEARS);
    }
}
