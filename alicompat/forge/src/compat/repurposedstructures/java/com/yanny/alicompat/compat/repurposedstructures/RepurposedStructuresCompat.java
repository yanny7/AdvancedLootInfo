package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.DetectRSLootTables;
import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.StructureModdedLootImporterApplier;
import com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class RepurposedStructuresCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return RepurposedStructuresLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, StructureSpecificMaps.TreasureMapForEmeralds.class, TreasureMapForEmeraldsAccessor.class);

        PluginUtils.registerConditionTooltip(registry, DetectRSLootTables.class, DetectRSLootTablesAccessor.class);

        PluginUtils.registerDestination(registry, StructureModdedLootImporterApplier.class, StructureModdedLootImporterApplierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, StructureModdedLootImporterApplier.class, StructureModdedLootImporterApplierAccessor.class);
    }
}
