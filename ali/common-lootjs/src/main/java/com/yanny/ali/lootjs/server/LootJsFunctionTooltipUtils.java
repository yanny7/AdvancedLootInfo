package com.yanny.ali.lootjs.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.DropExperienceFunction;
import com.yanny.ali.lootjs.modifier.ExplodeFunction;
import com.yanny.ali.lootjs.modifier.LightningStrikeFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.modifier.PreserveComponentsFunction;
import com.yanny.ali.plugin.server.TooltipUtils;
import org.jetbrains.annotations.NotNull;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static TooltipBuilder customPlayerTooltip(IServerUtils ignoredUtils, CustomPlayerFunction ignoredFunction) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.error(TooltipBuilder.translate(Lang.Error.DETAIL_NOT_AVAILABLE.singular()))),
                Lang.Functions.PLAYER_ACTION);
    }

    @NotNull
    public static TooltipBuilder preserveComponentsTooltip(IServerUtils utils, PreserveComponentsFunction function) {
        return utils.getValueTooltip(utils, function.getTypes()).key(Lang.Functions.PRESERVE_COMPONENTS);
    }

    @NotNull
    public static TooltipBuilder dropExperienceTooltip(IServerUtils utils, DropExperienceFunction function) {
        return TooltipBuilder.array((b) -> b.add(TooltipUtils.getIntNumberTooltip(utils, function.amount).build(Lang.Value.AMOUNT)), Lang.Functions.DROP_EXPERIENCE);
    }

    @NotNull
    public static TooltipBuilder explodeTooltip(IServerUtils utils, ExplodeFunction function) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, function.radius).build(Lang.Value.RADIUS));
            b.add(utils.getValueTooltip(utils, function.mode).build(Lang.Value.BLOCK_INTERACTION));
            b.add(utils.getValueTooltip(utils, function.fire).build(Lang.Value.BLOCKS_ON_FIRE));
        }, Lang.Functions.EXPLODE);
    }

    @NotNull
    public static TooltipBuilder lightningStrikeTooltip(IServerUtils utils, LightningStrikeFunction function) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, function.damageEntity).build(Lang.Value.DAMAGE_ENTITY)), Lang.Functions.LIGHTNING_STRIKE);
    }

    @NotNull
    public static TooltipBuilder modifiedItemTooltip(IServerUtils ignoredUtils, ModifiedItemFunction ignoredFunction) {
        return TooltipBuilder.error(TooltipBuilder.translate(Lang.Error.MODIFIED_ITEM.singular()));
    }
}
