package com.yanny.awi.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreBranchTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class BranchTooltipNode extends CoreBranchTooltipNode<IServerUtils, ITooltipNode> implements ITooltipNode {
    public static final Identifier ID = Utils.modLoc("branch");
    private static final LoadingCache<CacheKey<ITooltipNode>, BranchTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(BranchTooltipNode::new));

    private BranchTooltipNode(CacheKey<ITooltipNode> cacheKey) {
        super(cacheKey);
    }

    @NotNull
    @Override
    public Identifier getId() {
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
        // TODO
    }

    public static class Builder extends CoreBranchTooltipNode.Builder<ITooltipNode, IKeyTooltipNode, BranchTooltipNode> implements IKeyTooltipNode {
        public Builder(boolean advancedTooltip) {
            super(advancedTooltip, (key) -> getFromCache(CACHE, key));
        }
    }
}
