package com.yanny.ali.plugin.common.tooltip;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LiteralTooltipNode implements ITooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "literal");
    private static final LoadingCache<String, LiteralTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(text1 -> text1 != null ? new LiteralTooltipNode(text1) : null));

    private final String text;

    private LiteralTooltipNode(String text) {
        this.text = text;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LiteralTooltipNode that = (LiteralTooltipNode) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(text);
    }

    @Override
    public String toString() {
        return "LiteralTooltipNode{" +
                "text='" + text + '\'' +
                '}';
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
    public static LiteralTooltipNode decode(IClientUtils ignoredUtils, FriendlyByteBuf buf) {
        String text = buf.readUtf();
        try {
            return CACHE.get(text);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
