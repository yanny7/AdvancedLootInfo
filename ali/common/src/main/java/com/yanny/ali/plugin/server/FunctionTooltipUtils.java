package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static TooltipBuilder getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.enchantment).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, fun.formula).build(Lang.Value.FORMULA));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.APPLY_BONUS);
    }

    @NotNull
    public static TooltipBuilder getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.source).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_NAME);
    }

    @NotNull
    public static TooltipBuilder getCopyCustomDataTooltip(IServerUtils utils, CopyCustomDataFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.source).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, fun.operations).build(Lang.Branch.COPY_OPERATIONS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_CUSTOM_DATA);
    }

    @NotNull
    public static TooltipBuilder getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.block).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, fun.properties).build(Lang.Branch.PROPERTIES));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_STATE);
    }

    @NotNull
    public static TooltipBuilder getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, fun.onlyCompatible).build(Lang.Value.ONLY_COMPATIBLE));
            b.add(utils.getValueTooltip(utils, fun.includeAdditionalCostComponent).build(Lang.Value.INCLUDE_ADDITIONAL_COST_COMPONENT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.ENCHANT_RANDOMLY);
    }

    @NotNull
    public static TooltipBuilder getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.levels).build(Lang.Value.LEVELS));
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Branch.OPTIONS));
            b.add(utils.getValueTooltip(utils, fun.includeAdditionalCostComponent).build(Lang.Value.INCLUDE_ADDITIONAL_COST_COMPONENT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.ENCHANT_WITH_LEVELS);
    }

    @NotNull
    public static TooltipBuilder getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.destination).build(Lang.Value.DESTINATION));
            b.add(utils.getValueTooltip(utils, fun.mapDecoration).build(Lang.Value.MAP_DECORATION));
            b.add(utils.getValueTooltip(utils, fun.zoom).build(Lang.Value.ZOOM));
            b.add(utils.getValueTooltip(utils, fun.searchRadius).build(Lang.Value.SEARCH_RADIUS));
            b.add(utils.getValueTooltip(utils, fun.skipKnownStructures).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.EXPLORATION_MAP);
    }

    @NotNull
    public static TooltipBuilder getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS)))
                .showEmpty().key(Lang.Functions.EXPLOSION_DECAY);
    }

    @NotNull
    public static TooltipBuilder getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.entityTarget).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.FILL_PLAYER_HEAD);
    }

    @NotNull
    public static TooltipBuilder getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.useInputCount).build(Lang.Value.USE_INPUT_COUNT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.FURNACE_SMELT);
    }

    @NotNull
    public static TooltipBuilder getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.limit).build(Lang.Value.LIMIT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.LIMIT_COUNT);
    }

    @NotNull
    public static TooltipBuilder getEnchantedCountIncreaseTooltip(IServerUtils utils, EnchantedCountIncreaseFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.enchantment).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, fun.count).build(Lang.Value.COUNT));

            if (fun.limit > 0) {
                b.add(utils.getValueTooltip(utils, fun.limit).build(Lang.Value.LIMIT));
            }

            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.ENCHANTED_COUNT_INCREASE);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.REFERENCE);
    }

    @NotNull
    public static TooltipBuilder getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fun.functions)))
                .key(Lang.Functions.SEQUENCE);
    }

    @NotNull
    public static TooltipBuilder getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.modifiers).build(Lang.Branch.MODIFIERS));
            b.add(utils.getValueTooltip(utils, fun.replace).build(Lang.Value.REPLACE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_ATTRIBUTES);
    }

    @NotNull
    public static TooltipBuilder getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.append).build(Lang.Value.APPEND));
            b.add(utils.getValueTooltip(utils, fun.patterns).build(Lang.Branch.BANNER_PATTERNS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_BANNER_PATTERN);
    }

    @NotNull
    public static TooltipBuilder getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.component).build(Lang.Value.CONTAINER));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
            // TODO entries
        }).key(Lang.Functions.SET_CONTENTS);
    }

    @NotNull
    public static TooltipBuilder getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.count).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.SET_COUNT);
    }

    @NotNull
    public static TooltipBuilder getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.damage).build(Lang.Value.DAMAGE));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_DAMAGE);
    }

    @NotNull
    public static TooltipBuilder getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Branch.OPTIONS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_INSTRUMENT);
    }

    @NotNull
    public static TooltipBuilder getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.seed).build(Lang.Value.SEED));
            b.add(utils.getValueTooltip(utils, fun.type).build(Lang.Value.BLOCK_ENTITY_TYPE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_LOOT_TABLE);
    }

    @NotNull
    public static TooltipBuilder getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.mode).build(Lang.Value.LIST_OPERATION));
            b.add(utils.getValueTooltip(utils, fun.lore).build(Lang.Branch.LORE));
            b.add(utils.getValueTooltip(utils, fun.resolutionContext).build(Lang.Value.RESOLUTION_CONTEXT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_LORE);
    }

    @NotNull
    public static TooltipBuilder getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.resolutionContext).build(Lang.Value.RESOLUTION_CONTEXT));
            b.add(utils.getValueTooltip(utils, fun.target).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_NAME);
    }

    @NotNull
    public static TooltipBuilder getSetCustomDataTooltip(IServerUtils utils, SetCustomDataFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.tag.toString()).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_CUSTOM_DATA);
    }

    @NotNull
    public static TooltipBuilder getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.potion).build(Lang.Value.POTION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_POTION);
    }

    @NotNull
    public static TooltipBuilder getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        return TooltipBuilder.array((b) -> {
            if (!fun.effects.isEmpty()) {
                b.add(utils.getValueTooltip(utils, fun.effects));
            }

            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_STEW_EFFECT);
    }

    @NotNull
    public static TooltipBuilder getSetItemTooltip(IServerUtils utils, SetItemFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.item).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.SET_ITEM);
    }

    @NotNull
    public static TooltipBuilder getSetComponentsTooltip(IServerUtils utils, SetComponentsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.components).build(Lang.Branch.COMPONENTS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.SET_COMPONENTS);
    }

    @NotNull
    public static TooltipBuilder getModifyContentsTooltip(IServerUtils utils, ModifyContainerContents fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.component).build(Lang.Value.CONTAINER));
            b.add(TooltipBuilder.branch((c) -> c.add(utils.getValueTooltip(utils, fun.modifier))).build(Lang.Branch.MODIFIER));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.MODIFY_CONTENTS);
    }

    @NotNull
    public static TooltipBuilder getFilteredTooltip(IServerUtils utils, FilteredFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.filter).build(Lang.Branch.FILTER));
            b.add(utils.getValueTooltip(utils, fun.onPass).build(Lang.Branch.ON_PASS));
            b.add(utils.getValueTooltip(utils, fun.onFail).build(Lang.Branch.ON_FAIL));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.FILTERED);
    }

    @NotNull
    public static TooltipBuilder getCopyComponentsTooltip(IServerUtils utils, CopyComponentsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.source).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, fun.include).build(Lang.Branch.INCLUDE));
            b.add(utils.getValueTooltip(utils, fun.exclude).build(Lang.Branch.EXCLUDE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_COMPONENTS);
    }

    @NotNull
    public static TooltipBuilder getSetFireworksTooltip(IServerUtils utils, SetFireworksFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getStandaloneTooltip(utils, fun.explosions).build(Lang.Branch.EXPLOSIONS));
            b.add(utils.getValueTooltip(utils, fun.flightDuration).build(Lang.Value.FLIGHT_DURATION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_FIREWORKS);
    }

    @NotNull
    public static TooltipBuilder getSetFireworkExplosionTooltip(IServerUtils utils, SetFireworkExplosionFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.shape).build(Lang.Value.SHAPE));
            b.add(utils.getValueTooltip(utils, fun.colors).build(Lang.Value.COLORS));
            b.add(utils.getValueTooltip(utils, fun.fadeColors).build(Lang.Value.FADE_COLORS));
            b.add(utils.getValueTooltip(utils, fun.trail).build(Lang.Value.TRAIL));
            b.add(utils.getValueTooltip(utils, fun.twinkle).build(Lang.Value.TWINKLE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_FIREWORK_EXPLOSION);
    }

    @NotNull
    public static TooltipBuilder getSetBookCoverTooltip(IServerUtils utils, SetBookCoverFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.author).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, fun.title).build(Lang.Branch.TITLE));
            b.add(utils.getValueTooltip(utils, fun.generation).build(Lang.Value.GENERATION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_BOOK_COVER);
    }

    @NotNull
    public static TooltipBuilder getSetWrittenBookPagesTooltip(IServerUtils utils, SetWrittenBookPagesFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getFilterableTooltip(utils, Lang.Branch.PAGE, fun.pages).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, fun.pageOperation).build(Lang.Value.LIST_OPERATION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_WRITTEN_BOOK_PAGES);
    }

    @NotNull
    public static TooltipBuilder getSetWritableBookPagesTooltip(IServerUtils utils, SetWritableBookPagesFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getFilterableTooltip(utils, Lang.Branch.PAGE, fun.pages).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, fun.pageOperation).build(Lang.Value.LIST_OPERATION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_WRITABLE_BOOK_PAGES);
    }

    @NotNull
    public static TooltipBuilder getToggleTooltipsTooltip(IServerUtils utils, ToggleTooltips fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getMapTooltip(utils, fun.values, GenericTooltipUtils::getDataComponentEntryTooltip).build(Lang.Branch.COMPONENTS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.TOGGLE_TOOLTIPS);
    }

    @NotNull
    public static TooltipBuilder getSetOminousBottleAmplifierTooltip(IServerUtils utils, SetOminousBottleAmplifierFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.amplifier).build(Lang.Value.AMPLIFIER));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_OMINOUS_BOTTLE_AMPLIFIER);
    }

    @NotNull
    public static TooltipBuilder getSetCustomModelDataTooltip(IServerUtils utils, SetCustomModelDataFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.floats).build(Lang.Branch.FLOATS));
            b.add(utils.getValueTooltip(utils, fun.colors).build(Lang.Branch.COLORS));
            b.add(utils.getValueTooltip(utils, fun.flags).build(Lang.Branch.FLAGS));
            b.add(utils.getValueTooltip(utils, fun.strings).build(Lang.Branch.STRINGS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_CUSTOM_MODEL_DATA);
    }

    @NotNull
    public static TooltipBuilder getDiscardItemTooltip(IServerUtils utils, DiscardItem fun) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS)))
                .showEmpty()
                .key(Lang.Functions.DISCARD_ITEM);
    }

    @NotNull
    public static TooltipNode getSetRandomDyesTooltip(IServerUtils utils, SetRandomDyesFunction fun) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, fun.numberOfDyes).build("ali.property.value.number_of_dyes"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                )
                .build("ali.type.function.set_random_dyes");
    }

    @NotNull
    public static TooltipNode getSetRandomPotionsTooltip(IServerUtils utils, SetRandomPotionFunction fun) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, fun.options).build("ali.property.branch.options"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                )
                .build("ali.type.function.set_random_potions");
    }
}
