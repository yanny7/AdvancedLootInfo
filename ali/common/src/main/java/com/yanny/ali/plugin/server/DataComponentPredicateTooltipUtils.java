package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.component.predicates.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionPredicateTooltip;

public class DataComponentPredicateTooltipUtils {
    @NotNull
    public static TooltipBuilder getDamagePredicateTooltip(IServerUtils utils, DamagePredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.damage()).build(Lang.Value.DAMAGE));
            b.add(utils.getValueTooltip(utils, predicate.durability()).build(Lang.Value.DURABILITY));
        });
    }

    @NotNull
    public static TooltipBuilder getEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.Enchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key(Lang.Branch.ENCHANTMENT_PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getStoredEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.StoredEnchantments predicate) {
        return utils.getValueTooltip(utils, predicate.enchantments).key(Lang.Branch.ENCHANTMENT_PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getPotionsPredicateTooltip(IServerUtils utils, PotionsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.potions()).key(Lang.Branch.POTIONS);
    }

    @NotNull
    public static TooltipBuilder getCustomDataPredicateTooltip(IServerUtils utils, CustomDataPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.value()).key(Lang.Value.NBT)));
    }

    @NotNull
    public static TooltipBuilder getContainerPredicateTooltip(IServerUtils utils, ContainerPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.items()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getBundlePredicateTooltip(IServerUtils utils, BundlePredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.items()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getFireworksPredicateTooltip(IServerUtils utils, FireworksPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(getCollectionPredicateTooltip(utils, predicate.explosions()).build(Lang.Branch.EXPLOSIONS));
            b.add(utils.getValueTooltip(utils, predicate.flightDuration()).build(Lang.Value.FLIGHT_DURATION));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getWritableBookPredicateTooltip(IServerUtils utils, WritableBookPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.pages()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getWrittenBookPredicateTooltip(IServerUtils utils, WrittenBookPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(getCollectionPredicateTooltip(utils, predicate.pages()).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, predicate.author()).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, predicate.title()).build(Lang.Value.TITLE));
            b.add(utils.getValueTooltip(utils, predicate.generation()).build(Lang.Value.GENERATION));
            b.add(utils.getValueTooltip(utils, predicate.resolved()).build(Lang.Value.RESOLVED));
        });
    }

    @NotNull
    public static TooltipBuilder getAttributeModifiersPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate predicate) {
        return getCollectionPredicateTooltip(utils, predicate.modifiers()).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getTrimPredicateTooltip(IServerUtils utils, TrimPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.material()).build(Lang.Branch.MATERIALS));
            b.add(utils.getValueTooltip(utils, predicate.pattern()).build(Lang.Branch.PATTERNS));
        });
    }

    @NotNull
    public static TooltipBuilder getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayablePredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.song()).key(Lang.Branch.SONGS)));
    }

    @NotNull
    public static TooltipNode getAnyValueTooltip(IServerUtils utils, AnyValue predicate) {
        return utils.getValueTooltip(utils, predicate.type()).build("ali.property.value.type");
    }
}
