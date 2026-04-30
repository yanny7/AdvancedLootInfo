package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreBranchTooltipNode;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class BranchTooltipNode extends CoreBranchTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final ResourceLocation ID = Utils.modLoc("branch");
    private static final LoadingCache<CacheKey<ITooltipNode>, BranchTooltipNode> CACHE = CacheBuilder.newBuilder()
            .recordStats()
            .build(CacheLoader.from(BranchTooltipNode::new));

    private BranchTooltipNode(CacheKey<ITooltipNode> cacheKey) {
        super(cacheKey);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Builder branch() {
        return new Builder(false);
    }

    @NotNull
    public static Builder branch(boolean isAdvancedTooltip) {
        return new Builder(isAdvancedTooltip);
    }

    @NotNull
    public static BranchTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, BranchTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        if (utils.getConfiguration().logMoreStatistics) {
            CoreTooltipUtils.logCacheStatistics(CACHE, ID);
        }
    }

    public static class Builder extends CoreBranchTooltipNode.Builder<ITooltipNode, IKeyTooltipNode, BranchTooltipNode> implements IKeyTooltipNode {
        public Builder(boolean advancedTooltip) {
            super(advancedTooltip, (key) -> getFromCache(CACHE, key));
        }
    }
}
