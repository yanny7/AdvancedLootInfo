package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ErrorTooltipNode implements ITooltipNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "error");

    private final String value;

    private ErrorTooltipNode(String value) {
        this.value = value;
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(ITooltipNode.pad(pad, Component.translatable("ali.util.advanced_loot_info.missing", value).withStyle(ChatFormatting.RED)));
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(value);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ErrorTooltipNode that = (ErrorTooltipNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "ErrorTooltipNode{" +
                "value='" + value + '\'' +
                '}';
    }

    @NotNull
    public static ErrorTooltipNode.Builder error(String value) {
        return new ErrorTooltipNode.Builder(value);
    }

    @NotNull
    public static ErrorTooltipNode decode(IClientUtils ignoredUtils, RegistryFriendlyByteBuf buf) {
        String value = buf.readUtf();
        return new ErrorTooltipNode(value);
    }

    public static class Builder implements IKeyTooltipNode {
        private final String value;

        public Builder(String value) {
            this.value = value;
        }

        public Builder add(ITooltipNode node) {
            return this;
        }

        public ErrorTooltipNode build(String key) {
            return new ErrorTooltipNode(value);
        }

        public ErrorTooltipNode build() {
            return new ErrorTooltipNode(value);
        }
    }
}
