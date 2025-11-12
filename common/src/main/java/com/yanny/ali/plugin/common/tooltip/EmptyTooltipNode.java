package com.yanny.ali.plugin.common.tooltip;

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

public class EmptyTooltipNode implements ITooltipNode {
    public static final EmptyTooltipNode EMPTY = new EmptyTooltipNode();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "empty");

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "EmptyTooltipNode{}";
    }

    @NotNull
    public static Builder empty() {
        return new Builder();
    }

    public static EmptyTooltipNode decode(IClientUtils ignoredUtils, RegistryFriendlyByteBuf ignoredBuf) {
        return EMPTY;
    }

    public static class Builder extends ListTooltipNode.Builder {
        public EmptyTooltipNode build(String key) {
            return EmptyTooltipNode.EMPTY;
        }

        public EmptyTooltipNode build() {
            return EmptyTooltipNode.EMPTY;
        }
    }
}
