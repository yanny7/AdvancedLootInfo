package com.yanny.alicompat.compat.portinglib;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootContextExtensions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
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

        registry.registerLootContextPreparer(PortingLibLootCompat::prepareLootContext);

        registry.registerGlobalLootModifiers(PortingLibLootCompat::registerLootModifiers);
    }

    private static void prepareLootContext(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        ((LootContextExtensions) context).setQueriedLootTableId(page.tableId());
    }

    @NotNull
    private static List<IPageLootModifier> registerLootModifiers(IServerUtils utils) {
        return GlobalLootModifierCollector.collect(utils, LootModifierManagerAccessor.getAllLootMods().entrySet().stream().map(PortingLibLootCompat::wrap).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(Map.Entry<Identifier, IGlobalLootModifier> entry) {
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
