package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getItemDamagePredicateTooltip(IServerUtils utils, ItemDamagePredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_damage"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.damage", predicate.damage()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.durability", predicate.durability()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, "ali.type.item_sub_predicate.item_enchantments", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemStoredEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, "ali.type.item_sub_predicate.item_stored_enchantments", "ali.property.branch.enchantments", predicate.enchantments, GenericTooltipUtils::getEnchantmentPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemPotionsPredicateTooltip(IServerUtils utils, ItemPotionsPredicate predicate) {
        return getHolderSetTooltip(utils, "ali.type.item_sub_predicate.item_potions", "ali.property.value.null", predicate.potions(), RegistriesTooltipUtils::getPotionTooltip);
    }

    @NotNull
    public static ITooltipNode getItemCustomDataPredicateTooltip(IServerUtils utils, ItemCustomDataPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_custom_data"));
        
        tooltip.add(getNbtPredicateTooltip(utils, "ali.property.value.nbt", predicate.value()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemContainerPredicateTooltip(IServerUtils utils, ItemContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.type.item_sub_predicate.item_container", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemBundlePredicateTooltip(IServerUtils utils, ItemBundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.type.item_sub_predicate.item_bundle", "ali.property.branch.predicate", predicate.items(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemFireworkExplosionPredicateTooltip(IServerUtils utils, ItemFireworkExplosionPredicate predicate) {
        return getFireworkPredicateTooltip(utils, "ali.type.item_sub_predicate.item_firework_explosion", predicate.predicate());
    }

    @NotNull
    public static ITooltipNode getItemFireworksPredicateTooltip(IServerUtils utils, ItemFireworksPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_fireworks"));
        
        tooltip.add(getCollectionPredicateTooltip(utils, "ali.property.branch.explosions", "ali.property.branch.predicate", predicate.explosions(), GenericTooltipUtils::getFireworkPredicateTooltip));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.flight_duration", predicate.flightDuration()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemWritableBookPredicateTooltip(IServerUtils utils, ItemWritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.type.item_sub_predicate.item_writable_book", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemWrittenBookPredicateTooltip(IServerUtils utils, ItemWrittenBookPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_written_book"));
        
        tooltip.add(getCollectionPredicateTooltip(utils, "ali.property.branch.pages", "ali.property.value.page", predicate.pages(), GenericTooltipUtils::getPagePredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.author", predicate.author(), GenericTooltipUtils::getStringTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.title", predicate.title(), GenericTooltipUtils::getStringTooltip));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.generation", predicate.generation()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.resolved", predicate.resolved(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemAttributeModifiersPredicateTooltip(IServerUtils utils, ItemAttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.type.item_sub_predicate.item_attribute_modifiers", "ali.property.branch.modifier", predicate.modifiers(), GenericTooltipUtils::getEntryPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getItemTrimPredicateTooltip(IServerUtils utils, ItemTrimPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_trim"));
        
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.materials", "ali.property.value.null", predicate.material(), RegistriesTooltipUtils::getTrimMaterialTooltip));
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.patterns", "ali.property.value.null", predicate.pattern(), RegistriesTooltipUtils::getTrimPatternTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemJukeboxPlayableTooltip(IServerUtils utils, ItemJukeboxPlayablePredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.item_sub_predicate.item_jukebox_playable"));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.songs", "ali.property.value.null", predicate.song(), RegistriesTooltipUtils::getJukeboxSongTooltip));

        return tooltip;
    }
}
