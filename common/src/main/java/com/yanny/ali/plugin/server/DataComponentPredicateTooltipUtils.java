package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.core.component.predicates.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class DataComponentPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getDamagePredicateTooltip(IServerUtils utils, DamagePredicate predicate) {
        ITooltipNode components = new TooltipNode();

        components.add(getMinMaxBoundsTooltip(utils, "ali.property.value.damage", predicate.damage()));
        components.add(getMinMaxBoundsTooltip(utils, "ali.property.value.durability", predicate.durability()));

        return components;
    }

    @NotNull
    public static ITooltipNode getEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, "ali.property.branch.enchantment_predicate", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getStoredEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, "ali.property.branch.enchantment_predicate", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getPotionsPredicateTooltip(IServerUtils utils, PotionsPredicate predicate) {
        return getHolderSetTooltip(utils, "ali.property.branch.potions", "ali.property.value.null", predicate.potions(), RegistriesTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static ITooltipNode getCustomDataPredicateTooltip(IServerUtils utils, CustomDataPredicate predicate) {
        return getNbtPredicateTooltip(utils, "ali.property.value.nbt", predicate.value());
    }

    @NotNull
    public static ITooltipNode getContainerPredicateTooltip(IServerUtils utils, ContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getBundlePredicateTooltip(IServerUtils utils, BundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getFireworkExplosionPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate predicate) {
        return getFireworkPredicateTooltip(utils, "ali.property.branch.predicate", predicate.predicate());
    }

    @NotNull
    public static ITooltipNode getFireworksPredicateTooltip(IServerUtils utils, FireworksPredicate predicate) {
        ITooltipNode components = new TooltipNode();

        components.add(getCollectionPredicateTooltip(utils, "ali.property.branch.explosions", "ali.property.branch.predicate", predicate.explosions(), GenericTooltipUtils::getFireworkPredicateTooltip));
        components.add(getMinMaxBoundsTooltip(utils, "ali.property.value.flight_duration", predicate.flightDuration()));

        return components;
    }

    @NotNull
    public static ITooltipNode getWritableBookPredicateTooltip(IServerUtils utils, WritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getWrittenBookPredicateTooltip(IServerUtils utils, WrittenBookPredicate predicate) {
        ITooltipNode components = new TooltipNode();

        components.add(getCollectionPredicateTooltip(utils, "ali.property.branch.pages", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip));
        components.add(getOptionalTooltip(utils, "ali.property.value.author", predicate.author(), GenericTooltipUtils::getStringTooltip));
        components.add(getOptionalTooltip(utils, "ali.property.value.title", predicate.title(), GenericTooltipUtils::getStringTooltip));
        components.add(getMinMaxBoundsTooltip(utils, "ali.property.value.generation", predicate.generation()));
        components.add(getOptionalTooltip(utils, "ali.property.value.resolved", predicate.resolved(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static ITooltipNode getAttributeModifiersPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", "ali.property.branch.modifier", predicate.modifiers(), GenericTooltipUtils::getEntryPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getTrimPredicateTooltip(IServerUtils utils, TrimPredicate predicate) {
        ITooltipNode components = new TooltipNode();

        components.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.materials", "ali.property.value.null", predicate.material(), RegistriesTooltipUtils::getTrimMaterialTooltip));
        components.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.patterns", "ali.property.value.null", predicate.pattern(), RegistriesTooltipUtils::getTrimPatternTooltip));

        return components;
    }

    @NotNull
    public static ITooltipNode getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayablePredicate predicate) {
        return getOptionalHolderSetTooltip(utils, "ali.property.branch.songs", "ali.property.value.null", predicate.song(), RegistriesTooltipUtils::getJukeboxSongTooltip);
    }
}
