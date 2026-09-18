package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class RepurposedStructuresCompat implements IModCompat {
    static final String MOD_ID = "repurposed_structures";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, StructureSpecificMaps.TreasureMapForEmeralds.class, TreasureMapForEmeraldsAccessor.class);
    }
}
