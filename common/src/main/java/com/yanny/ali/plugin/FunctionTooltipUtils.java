package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.GenericTooltipUtils.pad;
import static com.yanny.ali.plugin.GenericTooltipUtils.translatable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(int pad, Holder<Enchantment> enchantment, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, enchantment, GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(GenericTooltipUtils.getFormulaTooltip(pad + 1, formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(int pad, CopyNameFunction.NameSource source) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(GenericTooltipUtils.getNameSourceTooltip(pad + 1, source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyCustomData(int pad, NbtProvider source, List<CopyCustomDataFunction.CopyOperation> operations) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_custom_data")));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad, "ali.property.value.source", source, GenericTooltipUtils::getNtbProviderTooltip));
        components.addAll(GenericTooltipUtils.getCollectionTooltip(pad + 1, "ali.property.branch.copy_operations", operations, GenericTooltipUtils::getCopyOperationTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(int pad, Holder<Block> block, Set<Property<?>> properties) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, block, GenericTooltipUtils::getBlockTooltip));
        components.addAll(GenericTooltipUtils.getCollectionTooltip(pad + 1, "ali.property.value.properties", properties, GenericTooltipUtils::getPropertyTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(int pad, Optional<HolderSet<Enchantment>> enchantments) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(GenericTooltipUtils.getOptionalHolderSetTooltip(pad + 1, "ali.property.branch.enchantments", enchantments, GenericTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(int pad, RangeValue levels, boolean treasure) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.levels", levels));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.treasure", treasure));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(int pad, TagKey<Structure> structure, Holder<MapDecorationType> mapDecoration, int zoom, int searchRadius, boolean skipKnownStructures) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.addAll(GenericTooltipUtils.getTagKeyTooltip(pad + 1, "ali.property.value.destination", structure));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.map_decoration", mapDecoration,
                (p, c) -> GenericTooltipUtils.getHolderTooltip(p, c, GenericTooltipUtils::getMapDecorationTypeTooltip)));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.zoom", zoom));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.search_radius", searchRadius));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.skip_known_structures", skipKnownStructures));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getExplosionDecayTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(int pad, LootContext.EntityTarget target) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.target", "target", Optional.of(target)));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(int pad, RangeValue min, RangeValue max) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.min", min));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.max", max));

        return components;
    }

    @NotNull
    public static List<Component> getLootingEnchantTooltip(int pad, RangeValue value, int limit) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.looting_enchant")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.value", value));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.limit", limit));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(int pad, ResourceKey<LootTable> name) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(GenericTooltipUtils.getResourceKeyTooltip(pad + 1, "ali.property.value.name", name));

        return components;
    }

    @NotNull
    public static List<Component> getSequenceTooltip(int pad, List<ILootFunction> functions) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.sequence")));
        components.addAll(GenericTooltipUtils.getFunctionsTooltip(pad + 1, functions));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(int pad, List<SetAttributesAliFunction.Modifier> modifiers) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(GenericTooltipUtils.getCollectionTooltip(pad + 1, "ali.property.branch.modifiers", modifiers, GenericTooltipUtils::getModifierTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(int pad, boolean append, BannerPatternLayers patterns) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.append", append));
        components.addAll(GenericTooltipUtils.getBannerPatternLayersTooltip(pad + 1, patterns));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(int pad, ContainerComponentManipulator<?> component) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(GenericTooltipUtils.getContainerComponentManipulatorTooltip(pad + 1, component));
        //FIXME .getEntries

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(int pad, RangeValue count, boolean add) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.count", count));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(int pad, RangeValue damage, boolean add) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.damage", damage));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(int pad, Map<Holder<Enchantment>, RangeValue> enchantments, boolean add) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            enchantments.forEach((enchantment, value) -> {
                components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 2, enchantment, GenericTooltipUtils::getEnchantmentTooltip));
                components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 3, "ali.property.value.levels", value));
            });
        }

        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(int pad, TagKey<Instrument> options) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(GenericTooltipUtils.getTagKeyTooltip(pad + 1, "ali.property.value.options", options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(int pad, ResourceLocation name, long seed, Holder<BlockEntityType<?>> blockEntityType) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(GenericTooltipUtils.getResourceLocationTooltip(pad + 1, "ali.property.value.name", name));
        components.addAll(GenericTooltipUtils.getLongTooltip(pad + 1, "ali.property.value.seed", seed));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, blockEntityType, GenericTooltipUtils::getBlockEntityTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(int pad, ListOperation mode, List<Component> lore, Optional<LootContext.EntityTarget> resolutionContext) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(GenericTooltipUtils.getListOperationTooltip(pad + 1, mode));
        components.addAll(GenericTooltipUtils.getCollectionTooltip(pad + 1, "ali.property.branch.lore", lore, (p, c) -> List.of(pad(pad + 2, c))));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.resolution_context", "target", resolutionContext));

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(int pad, Optional<Component> name, Optional<LootContext.EntityTarget> resolutionContext) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, "ali.property.value.name", name, GenericTooltipUtils::getComponentTooltip));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.resolution_context", "target", resolutionContext));

        return components;
    }

    @NotNull
    public static List<Component> getSetCustomDataTooltip(int pad, CompoundTag tag) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_data")));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.tag", tag.getAsString()));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(int pad, Holder<Potion> potion) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, potion, GenericTooltipUtils::getPotionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(int pad, Map<Holder<MobEffect>, RangeValue> effectMap) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        if (!effectMap.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.mob_effects")));
            effectMap.forEach((effect, duration) -> {
                components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 2, effect, GenericTooltipUtils::getMobEffectTooltip));
                components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 3, "ali.property.value.duration", duration));
            });
        }

        return components;
    }
}
