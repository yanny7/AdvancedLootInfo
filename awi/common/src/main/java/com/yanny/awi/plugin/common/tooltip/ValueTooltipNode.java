package com.yanny.awi.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreValueTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class ValueTooltipNode extends CoreValueTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("value");
    private static final LoadingCache<CacheKey<ITooltipNode>, ValueTooltipNode> CACHE = CacheBuilder.newBuilder()
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
    public static ValueTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, ValueTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        // TODO
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
