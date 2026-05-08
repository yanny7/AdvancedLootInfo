package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionPredicateTooltip;

public class ItemSubPredicateTooltipUtils {
    @NotNull
    public static TooltipNode getItemDamagePredicateTooltip(IServerUtils utils, ItemDamagePredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.damage()).build("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, predicate.durability()).build("ali.property.value.durability"))
        )
        .build("ali.type.item_sub_predicate.item_damage");
    }

    @NotNull
    public static TooltipNode getItemEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.Enchantments predicate) {
        return GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.enchantments", predicate.enchantments).build("ali.type.item_sub_predicate.item_enchantments");
    }

    @NotNull
    public static TooltipNode getItemStoredEnchantmentsPredicateTooltip(IServerUtils utils, ItemEnchantmentsPredicate.StoredEnchantments predicate) {
        return GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.enchantments", predicate.enchantments).build("ali.type.item_sub_predicate.item_stored_enchantments");
    }

    @NotNull
    public static TooltipNode getItemPotionsPredicateTooltip(IServerUtils utils, ItemPotionsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.potions()).build("ali.type.item_sub_predicate.item_potions");
    }

    @NotNull
    public static TooltipNode getItemCustomDataPredicateTooltip(IServerUtils utils, ItemCustomDataPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.value()).build("ali.property.value.nbt"))
        )
        .build("ali.type.item_sub_predicate.item_custom_data");
    }

    @NotNull
    public static TooltipNode getItemContainerPredicateTooltip(IServerUtils utils, ItemContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).build("ali.type.item_sub_predicate.item_container");
    }

    @NotNull
    public static TooltipNode getItemBundlePredicateTooltip(IServerUtils utils, ItemBundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).build("ali.type.item_sub_predicate.item_bundle");
    }

    @NotNull
    public static TooltipNode getItemFireworkExplosionPredicateTooltip(IServerUtils utils, ItemFireworkExplosionPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).build("ali.type.item_sub_predicate.item_firework_explosion");
    }

    @NotNull
    public static TooltipNode getItemFireworksPredicateTooltip(IServerUtils utils, ItemFireworksPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.explosions()).build("ali.property.branch.explosions"))
                .add(utils.getValueTooltip(utils, predicate.flightDuration()).build("ali.property.value.flight_duration"))
        )
        .build("ali.type.item_sub_predicate.item_fireworks");
    }

    @NotNull
    public static TooltipNode getItemWritableBookPredicateTooltip(IServerUtils utils, ItemWritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).build("ali.type.item_sub_predicate.item_writable_book");
    }

    @NotNull
    public static TooltipNode getItemWrittenBookPredicateTooltip(IServerUtils utils, ItemWrittenBookPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).build("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, predicate.author()).build("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, predicate.title()).build("ali.property.value.title"))
                .add(utils.getValueTooltip(utils, predicate.generation()).build("ali.property.value.generation"))
                .add(utils.getValueTooltip(utils, predicate.resolved()).build("ali.property.value.resolved"))
        )
        .build("ali.type.item_sub_predicate.item_written_book");
    }

    @NotNull
    public static TooltipNode getItemAttributeModifiersPredicateTooltip(IServerUtils utils, ItemAttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.modifier", predicate.modifiers()).build("ali.type.item_sub_predicate.item_attribute_modifiers");
    }

    @NotNull
    public static TooltipNode getItemTrimPredicateTooltip(IServerUtils utils, ItemTrimPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.material()).build("ali.property.branch.materials"))
                .add(utils.getValueTooltip(utils, predicate.pattern()).build("ali.property.branch.patterns"))
        )
        .build("ali.type.item_sub_predicate.item_trim");
    }

    @NotNull
    public static TooltipNode getItemJukeboxPlayableTooltip(IServerUtils utils, ItemJukeboxPlayablePredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.song()).build("ali.property.branch.songs"))
        )
        .build("ali.type.item_sub_predicate.item_jukebox_playable");
    }
}
