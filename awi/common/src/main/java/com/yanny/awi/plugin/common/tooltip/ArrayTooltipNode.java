package com.yanny.awi.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreArrayTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class ArrayTooltipNode extends CoreArrayTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("array");
    private static final LoadingCache<CacheKey<IServerUtils, ITooltipNode>, ArrayTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(ArrayTooltipNode::new));

    private ArrayTooltipNode(CacheKey<IServerUtils, ITooltipNode> cacheKey) {
        super(cacheKey);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ArrayTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, ArrayTooltipNode::new);
    }

    @NotNull
    public static Builder array() {
        return new Builder();
    }

    public static void logCacheStatistics(IServerUtils utils) {
        // TODO
    }

    public static class Builder extends CoreArrayTooltipNode.Builder<IServerUtils, ITooltipNode, ArrayTooltipNode> {
        public Builder() {
            super((key) -> getFromCache(CACHE, key));
        }
    }
}
