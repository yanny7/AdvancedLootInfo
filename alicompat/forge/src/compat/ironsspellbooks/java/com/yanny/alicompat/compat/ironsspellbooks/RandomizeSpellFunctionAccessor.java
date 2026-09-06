package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import com.yanny.alicompat.accessor.ReflectionUtils;
import io.redspace.ironsspellbooks.loot.RandomizeSpellFunction;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class RandomizeSpellFunctionAccessor extends BaseAccessor<RandomizeSpellFunction> implements IFunctionTooltip {
    @FieldAccessor
    private NumberProvider qualityRange;
    @FieldAccessor
    private SpellFilter applicableSpells;

    public RandomizeSpellFunctionAccessor(RandomizeSpellFunction parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, qualityRange).build(Lang.Description.QUALITY));
            b.add(utils.getValueTooltip(utils, applicableSpells).build(IronsSpellbooksLang.Branch.APPLICABLE_SPELLS));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, IronsSpellbooksLang.Functions.RANDOMIZE_SPELL);
    }

    public static TooltipBuilder getTooltip(IServerUtils utils, RandomizeSpellFunction function) {
        return ReflectionUtils.copyClassData(RandomizeSpellFunctionAccessor.class, function, RandomizeSpellFunction.class).getTooltip(utils);
    }
}
