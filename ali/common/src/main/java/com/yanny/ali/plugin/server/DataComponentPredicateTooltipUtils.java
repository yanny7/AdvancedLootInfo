package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.component.predicates.*;
import org.jetbrains.annotations.NotNull;

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
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.enchantments)));
    }

    @NotNull
    public static TooltipBuilder getStoredEnchantmentsPredicateTooltip(IServerUtils utils, EnchantmentsPredicate.StoredEnchantments predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.enchantments)));
    }

    @NotNull
    public static TooltipBuilder getPotionsPredicateTooltip(IServerUtils utils, PotionsPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.potions())));
    }

    @NotNull
    public static TooltipBuilder getCustomDataPredicateTooltip(IServerUtils utils, CustomDataPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.value()).build(Lang.Value.NBT)));
    }

    @NotNull
    public static TooltipBuilder getContainerPredicateTooltip(IServerUtils utils, ContainerPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.items())));
    }

    @NotNull
    public static TooltipBuilder getBundlePredicateTooltip(IServerUtils utils, BundlePredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.items())));
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.predicate())));
    }

    @NotNull
    public static TooltipBuilder getFireworksPredicateTooltip(IServerUtils utils, FireworksPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.explosions()).build(Lang.Branch.EXPLOSIONS));
            b.add(utils.getValueTooltip(utils, predicate.flightDuration()).build(Lang.Value.FLIGHT_DURATION));
        });
    }

    @NotNull
    public static TooltipBuilder getWritableBookPredicateTooltip(IServerUtils utils, WritableBookPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.pages())));
    }

    @NotNull
    public static TooltipBuilder getWrittenBookPredicateTooltip(IServerUtils utils, WrittenBookPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.pages()).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, predicate.author()).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, predicate.title()).build(Lang.Value.TITLE));
            b.add(utils.getValueTooltip(utils, predicate.generation()).build(Lang.Value.GENERATION));
            b.add(utils.getValueTooltip(utils, predicate.resolved()).build(Lang.Value.RESOLVED));
        });
    }

    @NotNull
    public static TooltipBuilder getAttributeModifiersPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.modifiers())));
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
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.song())));
    }
}
