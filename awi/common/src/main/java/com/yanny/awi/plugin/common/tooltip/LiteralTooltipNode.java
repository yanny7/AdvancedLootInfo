package com.yanny.awi.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.aci.tooltip.CoreLiteralTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getFromCache;

public class LiteralTooltipNode extends CoreLiteralTooltipNode<IServerUtils> implements ITooltipNode {
    public static final Identifier ID = Utils.modLoc("literal");
    private static final LoadingCache<String, LiteralTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((LiteralTooltipNode::new)));

    private LiteralTooltipNode(String text) {
        super(text);
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    @NotNull
    public static LiteralTooltipNode translatable(String text) {
        return getFromCache(CACHE, text);
    }

    @NotNull
    public static LiteralTooltipNode decode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        return decode(utils, buf, LiteralTooltipNode::new);
    }

    public static void logCacheStatistics(IServerUtils utils) {
        // TODO
    }
}
