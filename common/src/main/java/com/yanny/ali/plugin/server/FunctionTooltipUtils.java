package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.enchantment).build("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, fun.formula).build("ali.property.value.formula"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.apply_bonus");
    }

    @NotNull
    public static ITooltipNode getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.source).build("ali.property.value.source"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_name");
    }

    @NotNull
    public static ITooltipNode getCopyNbtTooltip(IServerUtils utils, CopyNbtFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.source.getType()).build("ali.property.value.nbt_provider"))
                .add(getCollectionTooltip(utils, "ali.property.branch.operation", fun.operations).build("ali.property.branch.operations"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_nbt");
    }

    @NotNull
    public static ITooltipNode getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.block).build("ali.property.value.block"))
                .add(utils.getValueTooltip(utils, fun.properties).build("ali.property.branch.properties"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_state");
    }

    @NotNull
    public static ITooltipNode getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.enchantments.isEmpty()) {
            tooltip = utils.getValueTooltip(utils, fun.enchantments);
        } else {
            tooltip = BranchTooltipNode.branch();
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.enchant_randomly");
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.levels).build("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, fun.treasure).build("ali.property.value.treasure"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.enchant_with_levels");
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.destination).build("ali.property.value.destination"))
                .add(utils.getValueTooltip(utils, fun.mapDecoration).build("ali.property.value.map_decoration"))
                .add(utils.getValueTooltip(utils, fun.zoom).build("ali.property.value.zoom"))
                .add(utils.getValueTooltip(utils, fun.searchRadius).build("ali.property.value.search_radius"))
                .add(utils.getValueTooltip(utils, fun.skipKnownStructures).build("ali.property.value.skip_known_structures"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.exploration_map");
    }

    @NotNull
    public static ITooltipNode getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.explosion_decay");
    }

    @NotNull
    public static ITooltipNode getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.entityTarget).build("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.fill_player_head");
    }

    @NotNull
    public static ITooltipNode getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.furnace_smelt");
    }

    @NotNull
    public static ITooltipNode getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.limiter).build("ali.property.value.limit"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.limit_count");
    }

    @NotNull
    public static ITooltipNode getLootingEnchantTooltip(IServerUtils utils, LootingEnchantFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.value"));

        if (fun.limit > 0) {
            tooltip.add(utils.getValueTooltip(utils, fun.limit).build("ali.property.value.limit"));
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.looting_enchant");
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.reference");
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.modifiers.isEmpty()) {
            tooltip = getCollectionTooltip(utils, "ali.property.branch.modifier", fun.modifiers);
        } else {
            tooltip = BranchTooltipNode.branch();
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.set_attributes");
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.append).build("ali.property.value.append"))
                .add(utils.getValueTooltip(utils, fun.patterns).build("ali.property.branch.banner_patterns"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_banner_pattern");
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.type).build("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_contents");
                // TODO entries
    }

    @NotNull
    public static ITooltipNode getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_count");
    }

    @NotNull
    public static ITooltipNode getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.damage).build("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_damage");
    }

    @NotNull
    public static ITooltipNode getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return BranchTooltipNode.branch()
                .add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).build("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_enchantments");
    }

    @NotNull
    public static ITooltipNode getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.options).build("ali.property.value.options"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_instrument");
    }

    @NotNull
    public static ITooltipNode getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.seed).build("ali.property.value.seed"))
                .add(utils.getValueTooltip(utils, fun.type).build("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_loot_table");
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.replace).build("ali.property.value.replace"))
                .add(utils.getValueTooltip(utils, fun.lore).build("ali.property.branch.lore"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_lore");
    }

    @NotNull
    public static ITooltipNode getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_name");
    }

    @NotNull
    public static ITooltipNode getSetNbtTooltip(IServerUtils utils, SetNbtFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.tag.getAsString()).build("ali.property.value.tag"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_nbt");
    }

    @NotNull
    public static ITooltipNode getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.potion).build("ali.property.value.potion"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_potion");
    }

    @NotNull
    public static ITooltipNode getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.effectDurationMap.isEmpty()) {
            tooltip = getMapTooltip(utils, fun.effectDurationMap, GenericTooltipUtils::getMobEffectDurationEntryTooltip);
        } else {
            tooltip = BranchTooltipNode.branch();
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.set_stew_effect");
    }
}
