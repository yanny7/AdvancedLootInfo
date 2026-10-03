package com.yanny.alicompat.compat.snowrealmagic;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.DynamicNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IModCompat;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import snownee.snow.loot.NormalizeLoot;

import java.util.List;

public class SnowRealMagicCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SnowRealMagicLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerEntry(NormalizeLoot.class, SnowRealMagicCompat::getNormalizeNode);
        registry.registerEntryTooltip(NormalizeLoot.class, SnowRealMagicCompat::getNormalizeTooltip);
    }

    @NotNull
    private static IDataNode getNormalizeNode(IServerUtils utils, NormalizeLoot entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions,
                                              List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(entry, conditions);
        LootCount chance = NodeUtils.getChance(utils, entry, rawChance, sumWeight, chanceConditions);

        return new DynamicNode(NodeUtils.toFloat(chance.value()), TooltipUtils.getDynamicTooltip(utils, entry.quality, chance, allFunctions, allConditions).build());
    }

    @NotNull
    private static TooltipBuilder getNormalizeTooltip(IServerUtils utils, NormalizeLoot entry) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipUtils.getWeightTooltip(entry.weight));
            b.add(TooltipUtils.getQualityTooltip(entry.quality));
            b.add(utils.getValueTooltip(utils, entry.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, entry.functions).build(Lang.Branch.MODIFIERS));
            b.showEmpty();
        }, SnowRealMagicLang.Entry.NORMALIZE);
    }
}
