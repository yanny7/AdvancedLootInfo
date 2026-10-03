package com.yanny.alicompat.compat.spellengine;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.spell_engine.rpg_series.loot.AffiliationGroupEntry;
import net.spell_engine.rpg_series.loot.LootConfig;
import net.spell_engine.spellbinding.SpellBindRandomlyLootFunction;
import org.jetbrains.annotations.NotNull;

public class SpellEngineCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SpellEngineLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntryWeight(registry, AffiliationGroupEntry.class, AffiliationGroupEntryAccessor.class);
        PluginUtils.registerEntryChildren(registry, AffiliationGroupEntry.class, AffiliationGroupEntryAccessor.class);
        PluginUtils.registerEntry(registry, AffiliationGroupEntry.class, AffiliationGroupEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, AffiliationGroupEntry.class, AffiliationGroupEntryAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, SpellBindRandomlyLootFunction.class, SpellBindRandomlyLootFunctionAccessor.class);
        registry.registerEnumTranslation(LootConfig.Behavior.WeightOperation.class, Utils.MOD_ID, SpellEngineLang.WEIGHT_OPERATION_OWNER);
    }
}
