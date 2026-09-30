package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpellScrollTrade {
    private static final int MIN_SURCHARGE = 4;
    private static final int MAX_SURCHARGE = 7;

    @NotNull
    public static WizardTrade of(SpellFilter filter, float minQuality, float maxQuality, ItemStack price, ItemStack forSale,
                                 int maxUses, int xp, float priceMultiplier) {
        List<AbstractSpell> spells = filter.getApplicableSpells().stream()
                .filter((spell) -> spell.isEnabled() && spell != SpellRegistry.none() && spell.allowLooting())
                .toList();
        Map<Integer, Double> levelWeights = new TreeMap<>();
        Map<Integer, Double> baseWeights = new TreeMap<>();

        for (AbstractSpell spell : spells) {
            int lowest = 1 + (int) (spell.getMaxLevel() * minQuality);
            int highest = Math.max(lowest, (int) ((spell.getMaxLevel() - 1) * maxQuality) + 1);
            double weight = 1.0 / spells.size() / (highest - lowest + 1);

            for (int level = lowest; level <= highest; level++) {
                levelWeights.merge(level, weight, Double::sum);
                baseWeights.merge(spell.getRarity(level).getValue() * 5 + level, weight, Double::sum);
            }
        }

        NumberExpr levels = levelWeights.isEmpty() ? NumberExpr.constant(1) : weighted(levelWeights);
        NumberExpr surcharge = NumberExpr.uniformInt(MIN_SURCHARGE, MAX_SURCHARGE);
        NumberExpr cost = baseWeights.isEmpty() ? surcharge : NumberExpr.add(weighted(baseWeights), surcharge);

        return WizardTrade.of(price, cost, new ItemStack(forSale.getItem()), NumberExpr.constant(1), maxUses, xp, priceMultiplier)
                .withResultTooltip((utils) -> TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, filter).build(IronsSpellbooksLang.Branch.SPELL_FILTER));
                    b.add(TooltipBuilder.number(levels).build(IronsSpellbooksLang.Value.SPELL_LEVEL));
                }, IronsSpellbooksLang.Functions.RANDOM_SPELL_SCROLL).build());
    }

    @NotNull
    private static NumberExpr weighted(Map<Integer, Double> weights) {
        return NumberExpr.weighted(weights.entrySet().stream()
                .map((e) -> new NumberExpr.WeightedEntry(e.getValue(), NumberExpr.constant(e.getKey())))
                .toList());
    }
}
