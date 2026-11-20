package com.yanny.alicompat.compat.grimoireofgaia;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import gaia.item.edible.consume_effects.ClearNegativeStatusEffectsConsumeEffect;
import gaia.registry.GaiaLootTables;
import gaia.registry.GaiaRegistry;
import org.jetbrains.annotations.NotNull;

public class GrimoireOfGaiaCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return GrimoireOfGaiaLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConsumeEffectTooltip(ClearNegativeStatusEffectsConsumeEffect.class, GrimoireOfGaiaCompat::getClearNegativeStatusEffectsTooltip);

        registry.registerEntityLootTable(GaiaRegistry.MINOTAURUS.getEntityType(), GaiaLootTables.MINOTAURUS_RANGED.identifier());
        registry.registerEntityLootTable(GaiaRegistry.DWARF.getEntityType(), GaiaLootTables.DWARF_RANGED.identifier());
        registry.registerEntityLootTable(GaiaRegistry.DWARF.getEntityType(), GaiaLootTables.DWARF_MINER.identifier());
        registry.registerEntityLootTable(GaiaRegistry.CHEST.getEntityType(), GaiaLootTables.CHEST_TABLES.identifier());
    }

    @NotNull
    private static TooltipBuilder getClearNegativeStatusEffectsTooltip(IServerUtils ignoredUtils, ClearNegativeStatusEffectsConsumeEffect ignoredEffect) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, GrimoireOfGaiaLang.ConsumeEffects.CLEAR_NEGATIVE_STATUS_EFFECTS);
    }
}
