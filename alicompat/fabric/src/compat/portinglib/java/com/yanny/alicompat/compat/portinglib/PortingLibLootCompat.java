package com.yanny.alicompat.compat.portinglib;

import com.google.gson.JsonElement;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PortingLibLootCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return "porting_lib_loot";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, LootTableIdCondition.class, LootTableIdConditionAccessor.class);

        PluginUtils.registerDestination(registry, LootTableIdCondition.class, LootTableIdConditionAccessor.class);

        registry.registerLootModifiers(PortingLibLootCompat::registerLootModifiers);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        return GlobalLootModifierCollector.collect(utils, LootModifierManagerAccessor.getAllLootMods().entrySet().stream().map(PortingLibLootCompat::wrap).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(Map.Entry<ResourceLocation, IGlobalLootModifier> entry) {
        IGlobalLootModifier modifier = entry.getValue();

        return new GlobalLootModifierWrapper(
                entry.getKey(),
                modifier,
                LootModifier.class,
                () -> Arrays.asList(ReflectionUtils.copyClassData(LootModifierAccessor.class, modifier, LootModifier.class).getConditions()),
                PortingLibLootCompat::serialize
        );
    }

    @NotNull
    private static JsonElement serialize() {
        throw new IllegalStateException("Not implemented");
    }
}
