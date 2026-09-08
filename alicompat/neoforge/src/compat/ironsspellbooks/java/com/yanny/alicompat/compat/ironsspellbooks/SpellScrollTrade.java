package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpellScrollTrade {
    private static final int MIN_SURCHARGE = 4;
    private static final int MAX_SURCHARGE = 7;

    @NotNull
    public static WizardTrade of(SpellFilter filter, float minQuality, float maxQuality, ItemStack price, ItemStack forSale,
                                 int maxUses, int xp, float priceMultiplier) {
        List<AbstractSpell> spells = filter.getApplicableSpells();
        int minPrice = MIN_SURCHARGE;
        int maxPrice = MAX_SURCHARGE;
        int minLevel = 1;
        int maxLevel = 1;

        for (AbstractSpell spell : spells) {
            int lowest = 1 + (int) (spell.getMaxLevel() * minQuality);
            int highest = Math.max(lowest, (int) ((spell.getMaxLevel() - 1) * maxQuality) + 1);

            for (int level = lowest; level <= highest; level++) {
                int base = spell.getRarity(Math.min(level, spell.getMaxLevel())).getValue() * 5 + level;

                minPrice = Math.min(minPrice, base + MIN_SURCHARGE);
                maxPrice = Math.max(maxPrice, base + MAX_SURCHARGE);
            }

            minLevel = Math.min(minLevel, lowest);
            maxLevel = Math.max(maxLevel, highest);
        }

        RangeValue levels = new RangeValue(minLevel, maxLevel);

        return WizardTrade.of(price, new RangeValue(minPrice, maxPrice), new ItemStack(forSale.getItem()), new RangeValue(1), maxUses, xp, priceMultiplier)
                .withResultTooltip((utils) -> TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, filter).build(IronsSpellbooksLang.Branch.SPELL_FILTER));
                    b.add(utils.getValueTooltip(utils, levels.toIntString()).build(IronsSpellbooksLang.Value.SPELL_LEVEL));
                }, IronsSpellbooksLang.Functions.RANDOM_SPELL_SCROLL).build());
    }
}
