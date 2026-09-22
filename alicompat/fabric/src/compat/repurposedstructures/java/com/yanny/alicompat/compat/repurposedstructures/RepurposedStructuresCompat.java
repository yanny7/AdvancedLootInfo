package com.yanny.alicompat.compat.repurposedstructures;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class RepurposedStructuresCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return RepurposedStructuresLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerGlobalLootModifiers(RepurposedStructuresCompat::registerLootModifiers);
    }

    @NotNull
    private static List<IPageLootModifier> registerLootModifiers(IServerUtils utils) {
        return Collections.singletonList(new StructureModdedLootImportModifier(utils));
    }
}
