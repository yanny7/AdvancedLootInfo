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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LiteralTooltipNode implements ITooltipNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "literal");
    private static final LoadingCache<String, LiteralTooltipNode> CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from((data) -> data != null ? new LiteralTooltipNode(data) : null));

    private final String text;

    private LiteralTooltipNode(String text) {
        this.text = text;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(text);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(ITooltipNode.pad(pad, Component.translatable(text).withStyle(TEXT_STYLE)));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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
    public static LiteralTooltipNode decode(IClientUtils ignoredUtils, RegistryFriendlyByteBuf buf) {
        String text = buf.readUtf();
        try {
            return CACHE.get(text);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
