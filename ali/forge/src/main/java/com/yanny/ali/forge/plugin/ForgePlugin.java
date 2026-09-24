package com.yanny.ali.forge.plugin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.*;
import com.yanny.ali.forge.mixin.MixinForgeInternalHandler;
import com.yanny.ali.forge.mixin.MixinLootModifier;
import com.yanny.ali.forge.mixin.MixinLootModifierManager;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.crafting.ingredients.*;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AliEntrypoint
public class ForgePlugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "forge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, ForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, ForgePlugin::getLootTableIdTooltip);

        registry.registerIngredientTooltip(CompoundIngredient.class, ForgeIngredientTooltipUtils::getCompoundIngredientTooltip);
        registry.registerIngredientTooltip(DifferenceIngredient.class, ForgeIngredientTooltipUtils::getDifferenceIngredientTooltip);
        registry.registerIngredientTooltip(IntersectionIngredient.class, ForgeIngredientTooltipUtils::getIntersectionIngredientTooltip);
        registry.registerIngredientTooltip(PartialNBTIngredient.class, ForgeIngredientTooltipUtils::getPartialNbtIngredientTooltip);
        registry.registerIngredientTooltip(StrictNBTIngredient.class, ForgeIngredientTooltipUtils::getStrictNbtIngredientTooltip);

        registry.registerLootContextPreparer(ForgePlugin::prepareLootContext);

        registry.registerGlobalLootModifiers(ForgePlugin::registerLootModifiers);
    }

    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.action().name())), Lang.Conditions.CAN_TOOL_PERFORM_ACTION);
    }

    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.id())), Lang.Conditions.LOOT_TABLE_ID);
    }

    private static void prepareLootContext(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        context.setQueriedLootTableId(page.tableId());
    }

    @NotNull
    private static List<IPageLootModifier> registerLootModifiers(IServerUtils utils) {
        MixinLootModifierManager manager = (MixinLootModifierManager) MixinForgeInternalHandler.getLootModifierManager();

        return GlobalLootModifierCollector.collect(utils, manager.getAliRegisteredLootModifiers().entrySet().stream().map((e) -> wrap(utils, e)).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IServerUtils utils, Map.Entry<ResourceLocation, IGlobalLootModifier> entry) {
        IGlobalLootModifier modifier = entry.getValue();

        return new GlobalLootModifierWrapper(
                entry.getKey(),
                modifier,
                LootModifier.class,
                () -> Arrays.asList(((MixinLootModifier) modifier).getAliConditions()),
                () -> serialize(utils, modifier)
        );
    }

    @NotNull
    private static JsonElement serialize(IServerUtils utils, IGlobalLootModifier modifier) {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
                //noinspection unchecked
                MapCodec<IGlobalLootModifier> codec = ((MapCodec<IGlobalLootModifier>) modifier.codec());
        return codec.codec().encodeStart(registryOps, modifier).getPartialOrThrow();
    }
}
