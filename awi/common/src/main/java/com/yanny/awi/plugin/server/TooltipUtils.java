package com.yanny.awi.plugin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.language.IMultiKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TooltipUtils {
    private static final int RANGE_LINE_CHARS = 40;
    private static final Comparator<BlockInfo> BLOCK_INFO_ORDER = Comparator
            .comparing((BlockInfo info) -> switch (info.storageType()) {
                case LAYERED -> 0;
                case ABSOLUTE -> 1;
                case RELATIVE -> 2;
            })
            .thenComparing((info) -> info.placement() == BlockInfo.Placement.CEILING)
            .thenComparing((info) -> info.ranges().isEmpty() ? 0 : info.ranges().get(0).min());

    public static TooltipBuilder getJsonTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e).rawKey(key))));
        }

        return getElementTooltip(utils, element);
    }

    @NotNull
    public static TooltipBuilder getBlockInfosTooltip(IServerUtils utils, List<BlockInfo> infos) {
        List<BlockInfo> ordered = infos.stream().sorted(BLOCK_INFO_ORDER).toList();

        return TooltipBuilder.array((b) -> {
            for (int i = 0; i < ordered.size(); i++) {
                if (i > 0) {
                    b.add(TooltipBuilder.keyOnly(Lang.BaseTerrain.ENTRY_SEPARATOR).build());
                }

                b.add(getBlockInfoTooltip(utils, ordered.get(i)));
            }
        });
    }

    @NotNull
    public static TooltipBuilder getBlockInfoTooltip(IServerUtils utils, BlockInfo info) {
        return TooltipBuilder.array((b) -> {
            BlockInfo.StorageType storage = info.storageType();

            addRanges(b, info.ranges(), storageLabel(storage), storageKey(storage, false), storageKey(storage, true), qualifier(info));

            if (!info.heights().isEmpty()) {
                addRanges(b, info.heights(), Lang.BaseTerrain.AT_Y, Lang.Value.AT_Y, Lang.Value.AT_Y, null);
            }

            if (info.layerShift() > 0) {
                b.add(utils.getValueTooltip(utils, "±" + info.layerShift()).build(Lang.Value.LAYER_SHIFT));
            }
        });
    }

    private static void addRanges(TooltipBuilder b, List<RangeValue> ranges, IMultiKey label, IMultiKey single,
                                  IMultiKey singleQualified, @Nullable String qualifier) {
        List<String> lines = wrapRanges(ranges);
        boolean wrapped = lines.size() > 1;

        if (wrapped) {
            b.add(TooltipBuilder.keyOnly(label).build());
        }

        for (int i = 0; i < lines.size(); i++) {
            boolean qualified = qualifier != null && i == lines.size() - 1;
            IMultiKey key;

            if (!wrapped) {
                key = qualified ? singleQualified : single;
            } else {
                key = qualified ? Lang.Value.CONTINUATION_QUALIFIED : Lang.Value.CONTINUATION;
            }

            if (qualified) {
                b.add(TooltipBuilder.value(lines.get(i), TooltipBuilder.translate(qualifier)).build(key));
            } else {
                b.add(TooltipBuilder.value(lines.get(i)).build(key));
            }
        }
    }

    @NotNull
    private static List<String> wrapRanges(List<RangeValue> ranges) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (RangeValue range : ranges) {
            String value = range.toIntString();

            if (!line.isEmpty() && line.length() + 2 + value.length() > RANGE_LINE_CHARS) {
                lines.add(line.append(',').toString());
                line = new StringBuilder();
            } else if (!line.isEmpty()) {
                line.append(", ");
            }

            line.append(value);
        }

        lines.add(line.toString());
        return lines;
    }

    @NotNull
    private static IMultiKey storageKey(BlockInfo.StorageType storageType, boolean qualified) {
        return switch (storageType) {
            case RELATIVE -> qualified ? Lang.Value.DEPTH_BELOW_SURFACE_QUALIFIED : Lang.Value.DEPTH_BELOW_SURFACE;
            case ABSOLUTE -> qualified ? Lang.Value.ABSOLUTE_Y_QUALIFIED : Lang.Value.ABSOLUTE_Y;
            case LAYERED -> qualified ? Lang.Value.LAYERS_AT_Y_QUALIFIED : Lang.Value.LAYERS_AT_Y;
        };
    }

    @NotNull
    private static IMultiKey storageLabel(BlockInfo.StorageType storageType) {
        return switch (storageType) {
            case RELATIVE -> Lang.BaseTerrain.DEPTH_BELOW_SURFACE;
            case ABSOLUTE -> Lang.BaseTerrain.ABSOLUTE_Y;
            case LAYERED -> Lang.BaseTerrain.LAYERS_AT_Y;
        };
    }

    @Nullable
    private static String qualifier(BlockInfo info) {
        boolean ceiling = info.placement() == BlockInfo.Placement.CEILING;

        return switch (info.water()) {
            case UNDERWATER -> (ceiling ? Lang.Placement.UNDERWATER_ON_CEILING : Lang.Placement.UNDERWATER).singular();
            case DRY -> (ceiling ? Lang.Placement.ON_LAND_ON_CEILING : Lang.Placement.ON_LAND).singular();
            case ANY -> ceiling ? Lang.Placement.ON_CEILING.singular() : null;
        };
    }

    private static TooltipBuilder getElementTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e).rawKey(key))));
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
