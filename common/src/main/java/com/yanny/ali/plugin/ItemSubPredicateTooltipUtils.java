package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static List<Component> getItemDamagePredicateTooltip(IUtils utils, int pad, ItemDamagePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_damage")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.damage", predicate.damage()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.durability", predicate.durability()));

        return components;
    }

    @NotNull
    public static List<Component> getItemEnchantmentsPredicateTooltip(IUtils utils, int pad, ItemEnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemStoredEnchantmentsPredicateTooltip(IUtils utils, int pad, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_stored_enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemPotionsPredicateTooltip(IUtils utils, int pad, ItemPotionsPredicate predicate) {
        return getHolderSetTooltip(utils, pad, "ali.type.item_sub_predicate.item_potions", predicate.potions(), GenericTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static List<Component> getItemCustomDataPredicateTooltip(IUtils utils, int pad, ItemCustomDataPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_custom_data")));
        components.addAll(getNbtPredicateTooltip(utils, pad + 1, predicate.value()));

        return components;
    }

    @NotNull
    public static List<Component> getItemContainerPredicateTooltip(IUtils utils, int pad, ItemContainerPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, predicate.items(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.type.item_sub_predicate.item_container", p, GenericTooltipUtils::getItemPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemBundlePredicateTooltip(IUtils utils, int pad, ItemBundlePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, predicate.items(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.type.item_sub_predicate.item_bundle", p, GenericTooltipUtils::getItemPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemFireworkExplosionPredicateTooltip(IUtils utils, int pad, ItemFireworkExplosionPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_firework_explosion")));
        components.addAll(getFireworkPredicateTooltip(utils, pad + 1, predicate.predicate()));

        return components;
    }

    @NotNull
    public static List<Component> getItemFireworksPredicateTooltip(IUtils utils, int pad, ItemFireworksPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_fireworks")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.explosions(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.explosions", p, GenericTooltipUtils::getFireworkPredicateTooltip)));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.flight_duration", predicate.flightDuration()));

        return components;
    }

    @NotNull
    public static List<Component> getItemWritableBookPredicateTooltip(IUtils utils, int pad, ItemWritableBookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_writable_book")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.pages(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.pages", p, GenericTooltipUtils::getPagePredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemWrittenBookPredicateTooltip(IUtils utils, int pad, ItemWrittenBookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_written_book")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.pages(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.pages", p, GenericTooltipUtils::getPagePredicateTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.author", predicate.author(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.title", predicate.title(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.generation", predicate.generation()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.resolved", predicate.resolved(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemAttributeModifiersPredicateTooltip(IUtils utils, int pad, ItemAttributeModifiersPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_attribute_modifiers")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.modifiers(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.modifiers", p, GenericTooltipUtils::getEntryPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemTrimPredicateTooltip(IUtils utils, int pad, ItemTrimPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_trim")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.materials", predicate.material(), GenericTooltipUtils::getTrimMaterialTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.patterns", predicate.pattern(), GenericTooltipUtils::getTrimPatternTooltip));

        return components;
    }
}
