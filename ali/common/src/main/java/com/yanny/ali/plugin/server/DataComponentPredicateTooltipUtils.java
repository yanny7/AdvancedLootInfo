package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.component.predicates.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionPredicateTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionTooltip;

public class DataComponentPredicateTooltipUtils {
    @NotNull
    public static TooltipNode getDamagePredicateTooltip(IServerUtils utils, DamagePredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.damage()).build("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, predicate.durability()).build("ali.property.value.durability"))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.Enchantments predicate) {
        return getCollectionTooltip(utils, "ali.property.branch.enchantments", predicate.enchantments).build("ali.property.branch.enchantment_predicate");
    }

    @NotNull
    public static TooltipNode getStoredEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.StoredEnchantments predicate) {
        return getCollectionTooltip(utils, "ali.property.branch.enchantments", predicate.enchantments).build("ali.property.branch.enchantment_predicate");
    }

    @NotNull
    public static TooltipNode getPotionsPredicateTooltip(IServerUtils utils, PotionsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.potions()).build("ali.property.branch.potions");
    }

    @NotNull
    public static TooltipNode getCustomDataPredicateTooltip(IServerUtils utils, CustomDataPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.value()).build("ali.property.value.nbt");
    }

    @NotNull
    public static TooltipNode getContainerPredicateTooltip(IServerUtils utils, ContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).build("ali.property.branch.predicate");
    }

    @NotNull
    public static TooltipNode getBundlePredicateTooltip(IServerUtils utils, BundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.items()).build("ali.property.branch.predicate");
    }

    @NotNull
    public static TooltipNode getFireworkExplosionPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).build("ali.property.branch.predicate");
    }

    @NotNull
    public static TooltipNode getFireworksPredicateTooltip(IServerUtils utils, FireworksPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(getCollectionPredicateTooltip(utils, "ali.property.branch.predicate", predicate.explosions()).build("ali.property.branch.explosions"))
                .add(utils.getValueTooltip(utils, predicate.flightDuration()).build("ali.property.value.flight_duration"))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getWritableBookPredicateTooltip(IServerUtils utils, WritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).build("ali.property.branch.predicate");
    }

    @NotNull
    public static TooltipNode getWrittenBookPredicateTooltip(IServerUtils utils, WrittenBookPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(getCollectionPredicateTooltip(utils, "ali.property.value.page", predicate.pages()).build("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, predicate.author()).build("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, predicate.title()).build("ali.property.value.title"))
                .add(utils.getValueTooltip(utils, predicate.generation()).build("ali.property.value.generation"))
                .add(utils.getValueTooltip(utils, predicate.resolved()).build("ali.property.value.resolved"))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getAttributeModifiersPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, "ali.property.branch.modifier", predicate.modifiers()).build("ali.property.branch.predicate");
    }

    @NotNull
    public static TooltipNode getTrimPredicateTooltip(IServerUtils utils, TrimPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.material()).build("ali.property.branch.materials"))
                .add(utils.getValueTooltip(utils, predicate.pattern()).build("ali.property.branch.patterns"))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayablePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.song()).build("ali.property.branch.songs");
    }

    @NotNull
    public static TooltipNode getAnyValueTooltip(IServerUtils utils, AnyValue predicate) {
        return utils.getValueTooltip(utils, predicate.type()).build("ali.property.value.type");
    }
}
