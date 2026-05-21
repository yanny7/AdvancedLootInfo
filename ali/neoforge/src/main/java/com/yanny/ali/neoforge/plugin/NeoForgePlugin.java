package com.yanny.ali.neoforge.plugin;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.language.Lang;
import com.yanny.ali.neoforge.mixin.MixinAddTableLootModifier;
import com.yanny.ali.neoforge.mixin.MixinCanItemPerformAbility;
import com.yanny.ali.neoforge.mixin.MixinLootModifier;
import com.yanny.ali.neoforge.mixin.MixinLootTableIdCondition;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

@AliEntrypoint
public class NeoForgePlugin implements IPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    @Override
    public String getModId() {
        return "neoforge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanItemPerformAbility.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);

        registry.registerLootModifiers(NeoForgePlugin::registerLootModifiers);

        registry.registerValueTooltip(ItemAbility.class, NeoForgePlugin::getItemAbilityTooltip);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanItemPerformAbility condition) {
        MixinCanItemPerformAbility cond = (MixinCanItemPerformAbility) condition;
        return utils.getValueTooltip(utils, cond.getAbility()).key(Lang.Conditions.CAN_ITEM_PERFORM_ABILITY);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return utils.getValueTooltip(utils, cond.getTargetLootTableId()).key(Lang.Conditions.LOOT_TABLE_ID);
    }

    private static TooltipBuilder getItemAbilityTooltip(IServerUtils utils, ItemAbility ability) {
        return utils.getValueTooltip(utils, ability.name());
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

        LootModifierManager lootModifierManager = utils.getServerLevel()
                .getServer()
                .getServerResources()
                .managers()
                .getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);

        forgeRegistry.registerGlobalLootModifier(AddTableLootModifier.class, (u, m) -> {
            List<LootItemCondition> conditionList = Arrays.asList(((MixinLootModifier) m).getConditions());

            return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
                TooltipNode tooltip = TooltipBuilder.array((b) -> b
                                .add(TooltipBuilder.keyOnly(Lang.Group.ALL))
                                .add(utils.getValueTooltip(utils, c))
                        )
                        .build();
                IDataNode node = NodeUtils.getReferenceNode(utils, ((MixinAddTableLootModifier) m).getTable().identifier(), c, tooltip);
                return List.of(new IOperation.AddOperation((i) -> true, node));
            }, tablePredicate);
        });

        for (IGlobalLootModifier globalLootModifier : lootModifierManager.getSortedModifiers()) {
            IGlobalLootModifierWrapper wrapper = wrap(utils, globalLootModifier);

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
            public Identifier getTargetLootTableId(LootItemCondition condition) {
                return ((MixinLootTableIdCondition) condition).getTargetLootTableId();
            }
        };
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IServerUtils utils, IGlobalLootModifier modifier) {
        return new IGlobalLootModifierWrapper() {
            @Override
            public Identifier getName() {
                return NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.getKey(modifier.codec());
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
                return Arrays.asList(((MixinLootModifier) modifier).getConditions());
            }

            @Override
            public JsonElement serialize() {
                RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
                //noinspection unchecked
                MapCodec<IGlobalLootModifier> codec = ((MapCodec<IGlobalLootModifier>) modifier.codec());
                return codec.codec().encodeStart(registryOps, modifier).getPartialOrThrow();
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
