package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static TooltipBuilder getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.enchantment).build("ali.property.value.enchantment"))
                        .add(utils.getValueTooltip(utils, fun.formula).build("ali.property.value.formula"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .isAdvancedTooltip()
                .key("ali.type.function.apply_bonus");
    }

    @NotNull
    public static TooltipBuilder getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.source).build("ali.property.value.source"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.copy_name");
    }

    @NotNull
    public static TooltipBuilder getCopyNbtTooltip(IServerUtils utils, CopyNbtFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.source.getType()).build("ali.property.value.nbt_provider"))
                        .add(getCollectionTooltip(utils, "ali.property.branch.operation", fun.operations).build("ali.property.branch.operations"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.copy_nbt");
    }

    @NotNull
    public static TooltipBuilder getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.block).build("ali.property.value.block"))
                        .add(utils.getValueTooltip(utils, fun.properties).build("ali.property.branch.properties"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.copy_state");
    }

    @NotNull
    public static TooltipBuilder getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        return TooltipBuilder.array((b) -> {
                    if (!fun.enchantments.isEmpty()) {
                        b.add(utils.getValueTooltip(utils, fun.enchantments));
                    }

                    b.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
                })
                .showEmpty()
                .key("ali.type.function.enchant_randomly");
    }

    @NotNull
    public static TooltipBuilder getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.levels).build("ali.property.value.levels"))
                        .add(utils.getValueTooltip(utils, fun.treasure).build("ali.property.value.treasure"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.enchant_with_levels");
    }

    @NotNull
    public static TooltipBuilder getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.destination).build("ali.property.value.destination"))
                        .add(utils.getValueTooltip(utils, fun.mapDecoration).build("ali.property.value.map_decoration"))
                        .add(utils.getValueTooltip(utils, fun.zoom).build("ali.property.value.zoom"))
                        .add(utils.getValueTooltip(utils, fun.searchRadius).build("ali.property.value.search_radius"))
                        .add(utils.getValueTooltip(utils, fun.skipKnownStructures).build("ali.property.value.skip_known_structures"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.exploration_map");
    }

    @NotNull
    public static TooltipBuilder getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .showEmpty()
                .key("ali.type.function.explosion_decay");
    }

    @NotNull
    public static TooltipBuilder getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.entityTarget).build("ali.property.value.target"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.fill_player_head");
    }

    @NotNull
    public static TooltipBuilder getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .showEmpty()
                .key("ali.type.function.furnace_smelt");
    }

    @NotNull
    public static TooltipBuilder getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.limiter).build("ali.property.value.limit"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .isAdvancedTooltip()
                .key("ali.type.function.limit_count");
    }

    @NotNull
    public static TooltipBuilder getLootingEnchantTooltip(IServerUtils utils, LootingEnchantFunction fun) {
        return TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.value"));

                    if (fun.limit > 0) {
                        b.add(utils.getValueTooltip(utils, fun.limit).build("ali.property.value.limit"));
                    }

                    b.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
                })
                .showEmpty()
                .key("ali.type.function.looting_enchant");
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.reference");
    }

    @NotNull
    public static TooltipBuilder getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        return TooltipBuilder.array((b) -> {
            if (!fun.modifiers.isEmpty()) {
                b.add(getCollectionTooltip(utils, "ali.property.branch.modifier", fun.modifiers));
            }

            b.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
        }).key("ali.type.function.set_attributes");
    }

    @NotNull
    public static TooltipBuilder getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.append).build("ali.property.value.append"))
                        .add(utils.getValueTooltip(utils, fun.patterns).build("ali.property.branch.banner_patterns"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_banner_pattern");
    }

    @NotNull
    public static TooltipBuilder getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.type).build("ali.property.value.block_entity_type"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_contents");
                // TODO entries
    }

    @NotNull
    public static TooltipBuilder getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.count"))
                        .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .isAdvancedTooltip()
                .key("ali.type.function.set_count");
    }

    @NotNull
    public static TooltipBuilder getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.damage).build("ali.property.value.damage"))
                        .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_damage");
    }

    @NotNull
    public static TooltipBuilder getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).build("ali.property.branch.enchantments"))
                        .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_enchantments");
    }

    @NotNull
    public static TooltipBuilder getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.options).build("ali.property.value.options"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_instrument");
    }

    @NotNull
    public static TooltipBuilder getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                        .add(utils.getValueTooltip(utils, fun.seed).build("ali.property.value.seed"))
                        .add(utils.getValueTooltip(utils, fun.type).build("ali.property.value.block_entity_type"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_loot_table");
    }

    @NotNull
    public static TooltipBuilder getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.replace).build("ali.property.value.replace"))
                        .add(utils.getValueTooltip(utils, fun.lore).build("ali.property.branch.lore"))
                        .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_lore");
    }

    @NotNull
    public static TooltipBuilder getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                        .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_name");
    }

    @NotNull
    public static TooltipBuilder getSetNbtTooltip(IServerUtils utils, SetNbtFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.tag.getAsString()).build("ali.property.value.tag"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_nbt");
    }

    @NotNull
    public static TooltipBuilder getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.potion).build("ali.property.value.potion"))
                        .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                )
                .key("ali.type.function.set_potion");
    }

    @NotNull
    public static TooltipBuilder getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        return TooltipBuilder.array((b) -> {
                    if (!fun.effectDurationMap.isEmpty()) {
                        b.add(getMapTooltip(utils, fun.effectDurationMap, GenericTooltipUtils::getMobEffectDurationEntryTooltip));
                    }

                    b.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
                })
                .showEmpty()
                .key("ali.type.function.set_stew_effect");
    }
}
