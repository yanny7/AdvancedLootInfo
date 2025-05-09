package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.component.predicates.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class DataComponentPredicateTooltipUtils {
    @NotNull
    public static List<Component> getDamagePredicateTooltip(IClientUtils utils, int pad, DamagePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.damage", predicate.damage()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.durability", predicate.durability()));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentsPredicateTooltip(IClientUtils utils, int pad, EnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.enchantment_predicate", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getStoredEnchantmentsPredicateTooltip(IClientUtils utils, int pad, EnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.enchantment_predicate", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static List<Component> getPotionsPredicateTooltip(IClientUtils utils, int pad, PotionsPredicate predicate) {
        return getHolderSetTooltip(utils, pad, "ali.property.branch.potions", "ali.property.value.null", predicate.potions(), RegistriesTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static List<Component> getCustomDataPredicateTooltip(IClientUtils utils, int pad, CustomDataPredicate predicate) {
        return getNbtPredicateTooltip(utils, pad, "ali.property.value.nbt", predicate.value());
    }

    @NotNull
    public static List<Component> getContainerPredicateTooltip(IClientUtils utils, int pad, ContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.property.branch.predicate", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static List<Component> getBundlePredicateTooltip(IClientUtils utils, int pad, BundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.property.branch.predicate", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static List<Component> getFireworkExplosionPredicateTooltip(IClientUtils utils, int pad, FireworkExplosionPredicate predicate) {
        return getFireworkPredicateTooltip(utils, pad, "ali.property.branch.predicate", predicate.predicate());
    }

    @NotNull
    public static List<Component> getFireworksPredicateTooltip(IClientUtils utils, int pad, FireworksPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionPredicateTooltip(utils, pad, "ali.property.branch.explosions", "ali.property.branch.predicate", predicate.explosions(), GenericTooltipUtils::getFireworkPredicateTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.flight_duration", predicate.flightDuration()));

        return components;
    }

    @NotNull
    public static List<Component> getWritableBookPredicateTooltip(IClientUtils utils, int pad, WritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.property.branch.predicate", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip);
    }

    @NotNull
    public static List<Component> getWrittenBookPredicateTooltip(IClientUtils utils, int pad, WrittenBookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionPredicateTooltip(utils, pad, "ali.property.branch.pages", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.author", predicate.author(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.title", predicate.title(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.generation", predicate.generation()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.resolved", predicate.resolved(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getAttributeModifiersPredicateTooltip(IClientUtils utils, int pad, AttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, pad, "ali.property.branch.predicate", "ali.property.branch.modifier", predicate.modifiers(), GenericTooltipUtils::getEntryPredicateTooltip);
    }

    @NotNull
    public static List<Component> getTrimPredicateTooltip(IClientUtils utils, int pad, TrimPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.materials", "ali.property.value.null", predicate.material(), RegistriesTooltipUtils::getTrimMaterialTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.patterns", "ali.property.value.null", predicate.pattern(), RegistriesTooltipUtils::getTrimPatternTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getJukeboxPlayableTooltip(IClientUtils utils, int pad, JukeboxPlayablePredicate predicate) {
        return getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.songs", "ali.property.value.null", predicate.song(), RegistriesTooltipUtils::getJukeboxSongTooltip);
    }
}
