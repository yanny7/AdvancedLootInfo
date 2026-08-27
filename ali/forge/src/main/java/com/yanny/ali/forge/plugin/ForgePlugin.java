package com.yanny.ali.forge.plugin;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.forge.mixin.MixinBasicItemListing;
import com.yanny.ali.forge.mixin.MixinCanToolPerformAction;
import com.yanny.ali.forge.mixin.MixinForgeInternalHandler;
import com.yanny.ali.forge.mixin.MixinLootModifier;
import com.yanny.ali.forge.mixin.MixinLootTableIdCondition;
import com.yanny.ali.language.Lang;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.loot.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class ForgePlugin implements IPlugin {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

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

        registry.registerItemListing(BasicItemListing.class, ForgePlugin::getBasicItemListingNode);

        registry.registerLootModifiers(ForgePlugin::registerLootModifiers);
    }

    @NotNull
    public static ItemsToItemsNode getBasicItemListingNode(IServerUtils utils, BasicItemListing listing, TooltipNode condition) {
        MixinBasicItemListing accessor = (MixinBasicItemListing) listing;

        return new ItemsToItemsNode(
                utils,
                Either.left(accessor.getPrice()),
                new RangeValue(accessor.getPrice().getCount()),
                Either.left(accessor.getPrice2()),
                new RangeValue(accessor.getPrice2().getCount()),
                Either.left(accessor.getForSale()),
                new RangeValue(accessor.getForSale().getCount()),
                accessor.getMaxTrades(),
                accessor.getXp(),
                accessor.getPriceMult(),
                condition
        );
    }

    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return utils.getValueTooltip(utils, ((MixinCanToolPerformAction) cond).getAction().name()).key(Lang.Conditions.CAN_TOOL_PERFORM_ACTION);
    }

    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return utils.getValueTooltip(utils, ((MixinLootTableIdCondition) cond).getTargetLootTableId()).key(Lang.Conditions.LOOT_TABLE_ID);
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        Map<Class<?>, BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>>> glmMap = new HashMap<>();
        Set<Class<?>> missingGLM = new HashSet<>();
        List<ILootModifier<?>> lootModifiers = new ArrayList<>();
        ILootTableIdConditionPredicate tablePredicate = getLootTableIdConditionPredicate();
        IGlobalLootModifierPlugin.IRegistry forgeRegistry = getForgeRegistry(glmMap);

        for (IPlugin plugin : Services.getPlatform().getPlugins()) {
            if (plugin instanceof IForgePlugin forgePlugin) {
                forgePlugin.registerGlobalLootModifier(forgeRegistry, tablePredicate);
            }
        }

        LootModifierManager lootModifierManager = MixinForgeInternalHandler.getLootModifierManager();

        for (IGlobalLootModifier globalLootModifier : lootModifierManager.getAllLootMods()) {
            IGlobalLootModifierWrapper wrapper = wrap(globalLootModifier);

            try {
                BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>> getter = glmMap.get(globalLootModifier.getClass());

                if (getter != null) {
                    Optional<ILootModifier<?>> lootModifier = getter.apply(utils, globalLootModifier);

                    if (lootModifier.isPresent()) {
                        lootModifiers.add(lootModifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for GLM {}", wrapper.getName());
                    }
                } else {
                    Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getMissingGlobalLootModifier(utils, wrapper, tablePredicate);

                    missingGLM.add(globalLootModifier.getClass());

                    if (modifier.isPresent()) {
                        lootModifiers.add(modifier.get());
                    } else {
                        LOGGER.warn("Unable to locate destination for auto GLM {}", wrapper.getName());
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to add GLM with error {}", e.getMessage(), e);
            }
        }

        missingGLM.forEach((c) -> LOGGER.warn("Missing GLM for {}", c.getName()));

        return lootModifiers;
    }

    @NotNull
    private static ILootTableIdConditionPredicate getLootTableIdConditionPredicate() {
        return new ILootTableIdConditionPredicate() {
            @Override
            public boolean isLootTableIdCondition(LootItemCondition condition) {
                return condition instanceof LootTableIdCondition;
            }

            @Override
            public ResourceLocation getTargetLootTableId(LootItemCondition condition) {
                return ((MixinLootTableIdCondition) condition).getTargetLootTableId();
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IGlobalLootModifier modifier) {
        return new IGlobalLootModifierWrapper() {
            @Override
            public ResourceLocation getName() {
                return ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().getKey(modifier.codec());
            }

            @Override
            public Class<?> getLootModifierClass() {
                return LootModifier.class;
            }

            @Override
            public boolean isLootModifier() {
                return modifier instanceof LootModifier;
            }

            @Override
            public List<LootItemCondition> getConditions() {
                return Arrays.asList(((MixinLootModifier) modifier).getAliConditions());
            }

            @Override
            public JsonElement serialize() {
                //noinspection unchecked
                Codec<IGlobalLootModifier> codec = ((Codec<IGlobalLootModifier>) modifier.codec());
                return codec.encodeStart(JsonOps.INSTANCE, modifier).getOrThrow(false, (s) -> {});
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierPlugin.IRegistry getForgeRegistry(Map<Class<?>, BiFunction<IServerUtils, IGlobalLootModifier, Optional<ILootModifier<?>>>> glmMap) {
        return new IGlobalLootModifierPlugin.IRegistry() {
            @Override
            public <T> void registerGlobalLootModifier(Class<T> type, BiFunction<IServerUtils, T, Optional<ILootModifier<?>>> getter) {
                //noinspection unchecked
                glmMap.put(type, (u, t) -> getter.apply(u, (T) t));
            }
        };
    }
}
