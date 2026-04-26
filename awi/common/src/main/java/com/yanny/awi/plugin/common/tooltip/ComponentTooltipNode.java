package com.yanny.awi.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreComponentTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class ComponentTooltipNode extends CoreComponentTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("component");
    private static final LoadingCache<CacheKey<IServerUtils, ITooltipNode>, ComponentTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new ComponentTooltipNode(data) : null));

    private ComponentTooltipNode(CacheKey<IServerUtils, ITooltipNode> cacheKey) {
        super(cacheKey);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Builder values(Component... values) {
        return new Builder(Arrays.asList(values));
    }

    @NotNull
    public static ComponentTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        return decode(utils, buf, ComponentTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        // TODO
    }

    public static class Builder extends CoreComponentTooltipNode.Builder<IServerUtils, ITooltipNode, ComponentTooltipNode, IKeyTooltipNode> implements IKeyTooltipNode {
        public Builder(List<Component> values) {
            super(values, (key) -> getFromCache(CACHE, key));
        }
    }
}
