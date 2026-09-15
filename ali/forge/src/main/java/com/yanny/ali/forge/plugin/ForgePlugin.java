package com.yanny.ali.forge.plugin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.*;
import com.yanny.ali.forge.mixin.MixinForgeInternalHandler;
import com.yanny.ali.forge.mixin.MixinLootModifier;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.crafting.ingredients.CompoundIngredient;
import net.minecraftforge.common.crafting.ingredients.DifferenceIngredient;
import net.minecraftforge.common.crafting.ingredients.IntersectionIngredient;
import net.minecraftforge.common.crafting.ingredients.PartialNBTIngredient;
import net.minecraftforge.common.crafting.ingredients.StrictNBTIngredient;
import net.minecraftforge.common.loot.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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

        registry.registerDestination(LootTableIdCondition.class, ForgePlugin::getLootTableIdDestination);

        registry.registerLootModifiers(ForgePlugin::registerLootModifiers);
    }

    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.action().name())), Lang.Conditions.CAN_TOOL_PERFORM_ACTION);
    }

    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.id())), Lang.Conditions.LOOT_TABLE_ID);
    }

    @NotNull
    public static Destination getLootTableIdDestination(IServerUtils ignoredUtils, LootTableIdCondition cond) {
        return new Destination.Table(cond.id()::equals, true);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        return GlobalLootModifierCollector.collect(utils, MixinForgeInternalHandler.getLootModifierManager().getAllLootMods().stream().map((m) -> wrap(utils, m)).toList());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IServerUtils utils, IGlobalLootModifier modifier) {
        return new GlobalLootModifierWrapper(
                ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().getKey(modifier.codec()),
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
