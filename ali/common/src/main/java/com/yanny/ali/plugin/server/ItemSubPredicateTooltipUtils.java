package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionPredicateTooltip;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static TooltipBuilder getItemDamagePredicateTooltip(IServerUtils utils, ItemDamagePredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.damage()).build(Lang.Value.DAMAGE));
            b.add(utils.getValueTooltip(utils, predicate.durability()).build(Lang.Value.DURABILITY));
        }).key(Lang.ItemSubPredicates.DAMAGE);
    }

    @NotNull
    public static TooltipBuilder getItemEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.Enchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key(Lang.ItemSubPredicates.ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getItemStoredEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key(Lang.ItemSubPredicates.STORED_ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getItemPotionsPredicateTooltip(IServerUtils utils, ItemPotionsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.potions()).key(Lang.ItemSubPredicates.POTIONS);
    }

    @NotNull
    public static TooltipBuilder getItemCustomDataPredicateTooltip(IServerUtils utils, ItemCustomDataPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.value()).build(Lang.Value.NBT)))
                .key(Lang.ItemSubPredicates.CUSTOM_DATA);
    }

    @NotNull
    public static TooltipBuilder getItemContainerPredicateTooltip(IServerUtils utils, ItemContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.items()).key(Lang.ItemSubPredicates.CONTAINER);
    }

    @NotNull
    public static TooltipBuilder getItemBundlePredicateTooltip(IServerUtils utils, ItemBundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.items()).key(Lang.ItemSubPredicates.BUNDLE);
    }

    @NotNull
    public static TooltipBuilder getItemFireworkExplosionPredicateTooltip(IServerUtils utils, ItemFireworkExplosionPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.ItemSubPredicates.FIREWORK_EXPLOSION);
    }

    @NotNull
    public static TooltipBuilder getItemFireworksPredicateTooltip(IServerUtils utils, ItemFireworksPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(getCollectionPredicateTooltip(utils, predicate.explosions()).build(Lang.Branch.EXPLOSIONS));
            b.add(utils.getValueTooltip(utils, predicate.flightDuration()).build(Lang.Value.FLIGHT_DURATION));
        }).key(Lang.ItemSubPredicates.FIREWORKS);
    }

    @NotNull
    public static TooltipBuilder getItemWritableBookPredicateTooltip(IServerUtils utils, ItemWritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.pages()).key(Lang.ItemSubPredicates.WRITABLE_BOOK);
    }

    @NotNull
    public static TooltipBuilder getItemWrittenBookPredicateTooltip(IServerUtils utils, ItemWrittenBookPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(getCollectionPredicateTooltip(utils, predicate.pages()).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, predicate.author()).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, predicate.title()).build(Lang.Value.TITLE));
            b.add(utils.getValueTooltip(utils, predicate.generation()).build(Lang.Value.GENERATION));
            b.add(utils.getValueTooltip(utils, predicate.resolved()).build(Lang.Value.RESOLVED));
        }).key(Lang.ItemSubPredicates.WRITTEN_BOOK);
    }

    @NotNull
    public static TooltipBuilder getItemAttributeModifiersPredicateTooltip(IServerUtils utils, ItemAttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.modifiers()).key(Lang.ItemSubPredicates.ATTRIBUTE_MODIFIERS);
    }

    @NotNull
    public static TooltipBuilder getItemTrimPredicateTooltip(IServerUtils utils, ItemTrimPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.material()).build(Lang.Branch.MATERIALS));
            b.add(utils.getValueTooltip(utils, predicate.pattern()).build(Lang.Branch.PATTERNS));
        }).key(Lang.ItemSubPredicates.TRIM);
    }

    @NotNull
    public static TooltipBuilder getItemJukeboxPlayableTooltip(IServerUtils utils, ItemJukeboxPlayablePredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.song()).build(Lang.Branch.SONGS)))
                .key(Lang.ItemSubPredicates.JUKEBOX_PLAYABLE);
    }
}
