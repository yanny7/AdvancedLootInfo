package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreComponentTooltipNode;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class ComponentTooltipNode extends CoreComponentTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("component");
    private static final LoadingCache<CacheKey<ITooltipNode>, ComponentTooltipNode> CACHE = CacheBuilder.newBuilder()
            .recordStats()
            .build(CacheLoader.from((data) -> data != null ? new ComponentTooltipNode(data) : null));

    private ComponentTooltipNode(CacheKey<ITooltipNode> cacheKey) {
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
    public static ComponentTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, ComponentTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        if (utils.getConfiguration().logMoreStatistics) {
            CoreTooltipUtils.logCacheStatistics(CACHE, ID);
        }
    }

    public static class Builder extends CoreComponentTooltipNode.Builder<IServerUtils, ITooltipNode, ComponentTooltipNode, IKeyTooltipNode> implements IKeyTooltipNode {
        public Builder(List<Component> values) {
            super(values, (key) -> getFromCache(CACHE, key));
        }
    }
}
