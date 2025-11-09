package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LiteralTooltipNode implements ITooltipNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "literal");
    private static final LoadingCache<String, LiteralTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(LiteralTooltipNode::new));

    private final String text;

    private LiteralTooltipNode(String text) {
        this.text = text;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(text);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(ITooltipNode.pad(pad, Component.translatable(text).withStyle(TEXT_STYLE)));
    }

    @NotNull
    public static LiteralTooltipNode translatable(String text) {
        try {
            return CACHE.get(text);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static LiteralTooltipNode decode(IClientUtils ignoredUtils, RegistryFriendlyByteBuf buf) {
        String text = buf.readUtf();
        try {
            return CACHE.get(text);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
