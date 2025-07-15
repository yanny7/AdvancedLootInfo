package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.advancements.critereon.*;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getLightningBoltPredicateTooltip(IServerUtils utils, LightningBoltPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.lightning_bolt"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.fishing_hook"));
        
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.player"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", predicate.level()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.game_type", predicate.gameType(), GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.recipes", predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.advancements", predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.slime"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.size", predicate.size()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.raider"));
        
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_raid", predicate.hasRaid()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.is_captain", predicate.isCaptain()));

        return tooltip;
    }

    @NotNull
    public static <V> ITooltipNode getVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        ITooltipNode tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, "ali.property.value.type", predicate);

        if (predicate.variant instanceof Enum<?> variant) {
            tooltip.add(getEnumTooltip(utils, "ali.property.value.variant", variant));
        }

        return tooltip;
    }

    @NotNull
    public static <V> ITooltipNode getHolderVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        ITooltipNode tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, "ali.property.value.type", predicate);
        
        tooltip.add(getHolderSetTooltip(utils, "ali.property.branch.variants", "ali.property.value.variant", predicate.variants, (u, s, v) -> switch (v) {
            case CatVariant catVariant -> getCatVariantTooltip(u, s, catVariant);
            case PaintingVariant paintingVariant -> getPaintingVariantTooltip(u, s, paintingVariant);
            case FrogVariant frogVariant -> getFrogVariantTooltip(u, s, frogVariant);
            case WolfVariant wolfVariant -> getWolfVariantTooltip(u, s, wolfVariant);
            default -> TooltipNode.EMPTY;
        }));

        return tooltip;
    }
}
