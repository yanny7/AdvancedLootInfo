package com.yanny.awi.plugin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import org.jetbrains.annotations.NotNull;

public class TooltipUtils {
    public static TooltipBuilder getJsonTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e).rawKey(key))));
        }

        return getElementTooltip(utils, element);
    }

    @NotNull
    public static TooltipBuilder getBlockInfoTooltip(IServerUtils utils, NodeUtils.BlockInfo info) {
        return TooltipBuilder.array((b) -> {
            RangeValue fistValue = info.ranges().get(0);

            switch (info.storageType()) {
                case RELATIVE -> b.add(utils.getValueTooltip(utils, fistValue).build(Lang.Value.DEPTH_BELOW_SURFACE));
                case ABSOLUTE -> b.add(utils.getValueTooltip(utils, info.ranges()).build(Lang.Branch.ABSOLUTE_Y));
                case LAYERED -> b.add(utils.getValueTooltip(utils, info.ranges()).build(Lang.Branch.LAYERS_AT_Y));
            }

            switch (info.water()) {
                case UNDERWATER -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.UNDERWATER.singular())).build(Lang.Value.PLACEMENT));
                case DRY -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.ON_LAND.singular())).build(Lang.Value.PLACEMENT));
                case ANY -> {} // indifferent to the water level — no placement line
            }

            switch (info.placement()) {
                case CEILING -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.ON_CEILING.singular())).build(Lang.Value.PLACEMENT));
                case FLOOR, ANY -> {} // normal below-surface placement — no overhang line
            }
        });
    }

    private static TooltipBuilder getElementTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e)).rawKey(key)));
        } else if (element.isJsonArray()) {
            return TooltipBuilder.array((b) -> element.getAsJsonArray().forEach((e) -> b.add(getElementTooltip(utils, e))));
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();

            if (jsonPrimitive.isBoolean()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsBoolean());
            } else if (jsonPrimitive.isString()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsString());
            } else if (jsonPrimitive.isNumber()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsNumber());
            }
        }

        return TooltipBuilder.empty();
    }
}
