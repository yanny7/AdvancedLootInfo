package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpellFilterAccessor extends BaseAccessor<SpellFilter> implements IValueTooltip {
    @FieldAccessor
    private SchoolType schoolType;
    @FieldAccessor
    private List<AbstractSpell> spells;
    @FieldAccessor
    private boolean force;

    public SpellFilterAccessor(SpellFilter parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            if (schoolType != null) {
                b.add(utils.getValueTooltip(utils, schoolType.getDisplayName()).build(IronsSpellbooksLang.Value.SCHOOL));
            }

            if (!spells.isEmpty()) {
                b.add(TooltipBuilder.array((s) -> {
                    for (AbstractSpell spell : spells) {
                        s.add(utils.getValueTooltip(utils, TooltipBuilder.translate("spell." + IronsSpellbooksLang.MOD_ID + "." + spell.getSpellName())).build(IronsSpellbooksLang.Value.SPELL));
                    }
                }, IronsSpellbooksLang.Branch.SPELLS));
            }

            b.add(utils.getValueTooltip(utils, force).build(IronsSpellbooksLang.Value.FORCE));
        });
    }
}
