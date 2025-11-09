package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionPredicateTooltip;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getItemDamagePredicateTooltip(IServerUtils utils, ItemDamagePredicate predicate) {
        return BranchTooltipNode.branch("ali.type.item_sub_predicate.item_damage")
                .add(utils.getValueTooltip(utils, predicate.damage()).key("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, predicate.durability()).key("ali.property.value.durability"));
    }

    @NotNull
    public static ITooltipNode getItemEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.Enchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key("ali.type.item_sub_predicate.item_enchantments");
    }

    @NotNull
    public static ITooltipNode getItemStoredEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key("ali.type.item_sub_predicate.item_stored_enchantments");
    }

    @NotNull
    public static ITooltipNode getItemPotionsPredicateTooltip(IServerUtils utils, ItemPotionsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.potions()).key("ali.type.item_sub_predicate.item_potions");
    }

    @NotNull
    public static ITooltipNode getItemCustomDataPredicateTooltip(IServerUtils utils, ItemCustomDataPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.item_sub_predicate.item_custom_data")
                .add(utils.getValueTooltip(utils, predicate.value()).key("ali.property.value.nbt"));
    }

    @NotNull
    public static ITooltipNode getItemContainerPredicateTooltip(IServerUtils utils, ItemContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).key("ali.type.item_sub_predicate.item_container");
    }

    @NotNull
    public static ITooltipNode getItemBundlePredicateTooltip(IServerUtils utils, ItemBundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).key("ali.type.item_sub_predicate.item_bundle");
    }

    @NotNull
    public static ITooltipNode getItemFireworkExplosionPredicateTooltip(IServerUtils utils, ItemFireworkExplosionPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key("ali.type.item_sub_predicate.item_firework_explosion");
    }

    @NotNull
    public static ITooltipNode getItemFireworksPredicateTooltip(IServerUtils utils, ItemFireworksPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.item_sub_predicate.item_fireworks")
                .add(getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.explosions()).key("ali.property.branch.explosions"))
                .add(utils.getValueTooltip(utils, predicate.flightDuration()).key("ali.property.value.flight_duration"));
    }

    @NotNull
    public static ITooltipNode getItemWritableBookPredicateTooltip(IServerUtils utils, ItemWritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).key("ali.type.item_sub_predicate.item_writable_book");
    }

    @NotNull
    public static ITooltipNode getItemWrittenBookPredicateTooltip(IServerUtils utils, ItemWrittenBookPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.item_sub_predicate.item_written_book")
                .add(getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).key("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, predicate.author()).key("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, predicate.title()).key("ali.property.value.title"))
                .add(utils.getValueTooltip(utils, predicate.generation()).key("ali.property.value.generation"))
                .add(utils.getValueTooltip(utils, predicate.resolved()).key("ali.property.value.resolved"));
    }

    @NotNull
    public static ITooltipNode getItemAttributeModifiersPredicateTooltip(IServerUtils utils, ItemAttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.modifier", predicate.modifiers()).key("ali.type.item_sub_predicate.item_attribute_modifiers");
    }

    @NotNull
    public static ITooltipNode getItemTrimPredicateTooltip(IServerUtils utils, ItemTrimPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.item_sub_predicate.item_trim")
                .add(utils.getValueTooltip(utils, predicate.material()).key("ali.property.branch.materials"))
                .add(utils.getValueTooltip(utils, predicate.pattern()).key("ali.property.branch.patterns"));
    }
}
