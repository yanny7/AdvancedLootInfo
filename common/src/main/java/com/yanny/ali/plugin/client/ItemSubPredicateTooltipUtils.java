package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static List<Component> getItemDamagePredicateTooltip(IClientUtils utils, int pad, ItemDamagePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_damage")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.damage", predicate.damage()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.durability", predicate.durability()));

        return components;
    }

    @NotNull
    public static List<Component> getItemEnchantmentsPredicateTooltip(IClientUtils utils, int pad, ItemEnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_enchantments", "ali.property.value.enchantment", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemStoredEnchantmentsPredicateTooltip(IClientUtils utils, int pad, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.type.item_sub_predicate.item_stored_enchantments", "ali.property.value.enchantment", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemPotionsPredicateTooltip(IClientUtils utils, int pad, ItemPotionsPredicate predicate) {
        return getHolderSetTooltip(utils, pad, "ali.type.item_sub_predicate.item_potions", "ali.property.value.null", predicate.potions(), GenericTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static List<Component> getItemCustomDataPredicateTooltip(IClientUtils utils, int pad, ItemCustomDataPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_custom_data")));
        components.addAll(getNbtPredicateTooltip(utils, pad + 1, "ali.property.value.nbt", predicate.value()));

        return components;
    }

    @NotNull
    public static List<Component> getItemContainerPredicateTooltip(IClientUtils utils, int pad, ItemContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.type.item_sub_predicate.item_container", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemBundlePredicateTooltip(IClientUtils utils, int pad, ItemBundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.type.item_sub_predicate.item_bundle", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemFireworkExplosionPredicateTooltip(IClientUtils utils, int pad, ItemFireworkExplosionPredicate predicate) {
        return getFireworkPredicateTooltip(utils, pad, "ali.type.item_sub_predicate.item_firework_explosion", predicate.predicate());
    }

    @NotNull
    public static List<Component> getItemFireworksPredicateTooltip(IClientUtils utils, int pad, ItemFireworksPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_fireworks")));
        components.addAll(getCollectionPredicateTooltip(utils, pad + 1, "ali.property.branch.explosions", "ali.property.branch.predicate", predicate.explosions(), GenericTooltipUtils::getFireworkPredicateTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.flight_duration", predicate.flightDuration()));

        return components;
    }

    @NotNull
    public static List<Component> getItemWritableBookPredicateTooltip(IClientUtils utils, int pad, ItemWritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.type.item_sub_predicate.item_writable_book", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemWrittenBookPredicateTooltip(IClientUtils utils, int pad, ItemWrittenBookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_written_book")));
        components.addAll(getCollectionPredicateTooltip(utils, pad + 1, "ali.property.branch.pages", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.author", predicate.author(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.title", predicate.title(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.generation", predicate.generation()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.resolved", predicate.resolved(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemAttributeModifiersPredicateTooltip(IClientUtils utils, int pad, ItemAttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.type.item_sub_predicate.item_attribute_modifiers", "ali.property.branch.modifier", predicate.modifiers(), GenericTooltipUtils::getEntryPredicateTooltip);
    }

    @NotNull
    public static List<Component> getItemTrimPredicateTooltip(IClientUtils utils, int pad, ItemTrimPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_trim")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.materials", "ali.property.value.null", predicate.material(), GenericTooltipUtils::getTrimMaterialTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.patterns", "ali.property.value.null", predicate.pattern(), GenericTooltipUtils::getTrimPatternTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemJukeboxPlayableTooltip(IClientUtils utils, int pad, ItemJukeboxPlayablePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.item_sub_predicate.item_jukebox_playable")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.songs", predicate.song(), GenericTooltipUtils::getJukeboxSongTooltip));

        return components;
    }
}
