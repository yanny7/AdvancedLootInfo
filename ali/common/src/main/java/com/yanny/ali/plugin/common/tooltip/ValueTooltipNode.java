package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.CoreValueTooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class ValueTooltipNode extends CoreValueTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("value");
    private static final LoadingCache<CacheKey<ITooltipNode>, ValueTooltipNode> CACHE = CacheBuilder.newBuilder()
            .recordStats()
            .build(CacheLoader.from(ValueTooltipNode::new));

    private ValueTooltipNode(CacheKey<ITooltipNode> cacheKey) {
        super(cacheKey);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Builder value(Object... objects) {
        List<String> values;

        if (objects.length == 1) {
            values = Collections.singletonList(objects[0].toString());
        } else {
            values = new ArrayList<>(objects.length);

            for (Object value : objects) {
                values.add(value.toString());
            }
        }

        return new Builder(values);
    }

    @NotNull
    public static Builder keyValue(Object key, Object value) {
        return new Builder(key.toString(), value.toString());
    }

    @NotNull
    public static ValueTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        return decode(utils, buf, ValueTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        if (utils.getConfiguration().logMoreStatistics) {
            CoreTooltipUtils.logCacheStatistics(CACHE, ID);
        }
    }

    public static class Builder extends CoreValueTooltipNode.Builder<ITooltipNode, IKeyTooltipNode, ValueTooltipNode> implements IKeyTooltipNode {
        public Builder(List<String> values) {
            super(values, (k) -> getFromCache(CACHE, k));
        }

        public Builder(String key, String value) {
            super(key, value, (k) -> getFromCache(CACHE, k));
        }
    }
}
