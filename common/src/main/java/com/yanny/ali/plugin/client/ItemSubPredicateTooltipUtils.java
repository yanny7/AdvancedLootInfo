package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.component.predicates.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static List<Component> getItemDamagePredicateTooltip(IClientUtils utils, int pad, DamagePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_damage")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.damage", predicate.damage()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.durability", predicate.durability()));

        return components;
    }

    @NotNull
    public static List<Component> getItemEnchantmentsPredicateTooltip(IClientUtils utils, int pad, EnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemStoredEnchantmentsPredicateTooltip(IClientUtils utils, int pad, EnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_stored_enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemPotionsPredicateTooltip(IClientUtils utils, int pad, PotionsPredicate predicate) {
        return getHolderSetTooltip(utils, pad, "ali.type.item_sub_predicate.item_potions", predicate.potions(), GenericTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static List<Component> getItemCustomDataPredicateTooltip(IClientUtils utils, int pad, CustomDataPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_custom_data")));
        components.addAll(getNbtPredicateTooltip(utils, pad + 1, predicate.value()));

        return components;
    }

    @NotNull
    public static List<Component> getItemContainerPredicateTooltip(IClientUtils utils, int pad, ContainerPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, predicate.items(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.type.item_sub_predicate.item_container", p, GenericTooltipUtils::getItemPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemBundlePredicateTooltip(IClientUtils utils, int pad, BundlePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, predicate.items(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.type.item_sub_predicate.item_bundle", p, GenericTooltipUtils::getItemPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemFireworkExplosionPredicateTooltip(IClientUtils utils, int pad, FireworkExplosionPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_firework_explosion")));
        components.addAll(getFireworkPredicateTooltip(utils, pad + 1, predicate.predicate()));

        return components;
    }

    @NotNull
    public static List<Component> getItemFireworksPredicateTooltip(IClientUtils utils, int pad, FireworksPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_fireworks")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.explosions(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.explosions", p, GenericTooltipUtils::getFireworkPredicateTooltip)));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.flight_duration", predicate.flightDuration()));

        return components;
    }

    @NotNull
    public static List<Component> getItemWritableBookPredicateTooltip(IClientUtils utils, int pad, WritableBookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_writable_book")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.pages(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.pages", p, GenericTooltipUtils::getPagePredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemWrittenBookPredicateTooltip(IClientUtils utils, int pad, WrittenBookPredicate predicate) {
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
    public static List<Component> getItemAttributeModifiersPredicateTooltip(IClientUtils utils, int pad, AttributeModifiersPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_attribute_modifiers")));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.modifiers(),
                (u, i, p) ->
                        getCollectionPredicateTooltip(u, i, "ali.property.branch.modifiers", p, GenericTooltipUtils::getEntryPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getItemTrimPredicateTooltip(IClientUtils utils, int pad, TrimPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_trim")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.materials", predicate.material(), GenericTooltipUtils::getTrimMaterialTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.patterns", predicate.pattern(), GenericTooltipUtils::getTrimPatternTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemJukeboxPlayableTooltip(IClientUtils utils, int pad, JukeboxPlayablePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_jukebox_playable")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.songs", predicate.song(), GenericTooltipUtils::getJukeboxSongTooltip));

        return components;
    }
}
