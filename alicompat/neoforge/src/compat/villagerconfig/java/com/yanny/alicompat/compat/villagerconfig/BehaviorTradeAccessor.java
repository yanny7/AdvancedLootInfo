package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import me.drex.villagerconfig.common.data.BehaviorTrade;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BehaviorTradeAccessor extends BaseAccessor<BehaviorTrade> {
    @FieldAccessor
    private LootPoolEntryContainer costA;
    @FieldAccessor
    private Optional<LootPoolEntryContainer> costB;
    @FieldAccessor
    private LootPoolEntryContainer result;
    @FieldAccessor
    private NumberProvider priceMultiplier;
    @FieldAccessor
    private NumberProvider traderExperience;
    @FieldAccessor
    private NumberProvider maxUses;
    @FieldAccessor
    private List<LootItemCondition> conditions;
    @FieldAccessor
    private boolean rewardExperience;
    @FieldAccessor
    private Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments;

    public BehaviorTradeAccessor(BehaviorTrade parent) {
        super(parent);
    }

    @NotNull
    public IDataNode getNode(IServerUtils utils) {
        return new ItemsToItemsNode(
                TradeUtils.getSlotNode(utils, costA),
                costB.map((e) -> TradeUtils.getSlotNode(utils, e)).orElseGet(TradeUtils::getEmptySlotNode),
                TradeUtils.getSlotNode(utils, result),
                TooltipBuilder.array((b) -> {
                    b.add(TooltipUtils.getIntNumberTooltip(utils, maxUses).build(Lang.Value.USES));
                    b.add(TooltipUtils.getIntNumberTooltip(utils, traderExperience).build(Lang.Value.VILLAGER_XP));
                    b.add(TooltipUtils.getNumberTooltip(utils, priceMultiplier).build(VillagerConfigLang.Value.PRICE_MULTIPLIER));
                    b.add(utils.getValueTooltip(utils, rewardExperience).build(VillagerConfigLang.Value.REWARD_EXPERIENCE));
                    b.add(utils.getValueTooltip(utils, doubleTradePriceEnchantments).build(Lang.Branch.DOUBLE_TRADE_PRICE_ENCHANTMENTS));
                    b.add(utils.getValueTooltip(utils, conditions).build(Lang.Branch.PREDICATES));
                }).build()
        );
    }
}
