package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import com.yanny.alicompat.accessor.IItemStackModifier;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class AutomaticAffixTradeAccessor extends BaseAccessor<AutomaticAffixTrade> implements IFunctionTooltip, IItemStackModifier {
    @FieldAccessor
    private Set<LootRarity> rarities;

    @FieldAccessor
    private List<DynamicHolder<AffixLootEntry>> entries;

    public AutomaticAffixTradeAccessor(AutomaticAffixTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, ApotheosisLang.Functions.AUTOMATIC_AFFIX_TRADE);
    }

    @Override
    public ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack) {
        ItemStack affixStack = ApotheosisUtils.firstEntryStack(entries);

        return affixStack.isEmpty() ? itemStack : affixStack;
    }
}
