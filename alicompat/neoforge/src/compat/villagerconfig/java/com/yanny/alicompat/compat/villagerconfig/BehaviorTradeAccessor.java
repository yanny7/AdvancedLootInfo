package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import me.drex.villagerconfig.common.data.BehaviorTrade;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BehaviorTradeAccessor extends BaseAccessor<BehaviorTrade> implements IItemListing {
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
    private Map<String, NumberProvider> referenceProviders;

    public BehaviorTradeAccessor(BehaviorTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode condition) {
        return ReferenceLootNumberProviderAccessor.withReferences(referenceProviders, () -> new ItemsToItemsNode(
                TradeUtils.getSlotNode(utils, costA),
                costB.map((e) -> TradeUtils.getSlotNode(utils, e)).orElseGet(TradeUtils::getEmptySlotNode),
                TradeUtils.getSlotNode(utils, result),
                TooltipBuilder.array((b) -> {
                    b.add(condition);
                    b.add(TooltipUtils.getIntNumberTooltip(utils, maxUses).build(Lang.Value.USES));
                    b.add(TooltipUtils.getIntNumberTooltip(utils, traderExperience).build(Lang.Value.XP));
                    b.add(TooltipUtils.getNumberTooltip(utils, priceMultiplier).build(Lang.Value.PRICE_MULTIPLIER));
                    b.add(utils.getValueTooltip(utils, conditions).build(Lang.Branch.PREDICATES));
                }).build()
        ));
    }
}
