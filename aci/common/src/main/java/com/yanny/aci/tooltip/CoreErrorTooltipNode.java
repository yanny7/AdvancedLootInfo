package com.yanny.aci.tooltip;

import com.yanny.aci.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreErrorTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> implements ICoreTooltipNode<SU>, ICoreKeyTooltipNode<SU, TN, KTN> {
    private final String value;

    protected CoreErrorTooltipNode(String value) {
        this.value = value;
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(pad(pad, Component.translatable("ali.util.advanced_loot_info.missing", value).withStyle(ChatFormatting.RED)));
    }

    @Override
    public final void encode(SU utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        //noinspection unchecked
        CoreErrorTooltipNode<SU, TN, KTN> that = (CoreErrorTooltipNode<SU, TN, KTN>) o;
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
    @Override
    public KTN add(TN node) {
        //noinspection unchecked
        return (KTN) this;
    }

    @NotNull
    @Override
    public TN build(String key) {
        //noinspection unchecked
        return (TN) this;
    }

    @NotNull
    protected static <
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>,
            T extends ICoreTooltipNode<SU>
    > T decode(CU ignoredUtils, FriendlyByteBuf buf, Function<String, T> factory) {
        String value = buf.readUtf();
        return factory.apply(value);
    }

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> extends CoreListTooltipNode.Builder<SU, TN, KTN> {
        private final String value;
        private final Function<String, CoreErrorTooltipNode<SU, TN, KTN>> factory;

        public Builder(String value, Function<String, CoreErrorTooltipNode<SU, TN, KTN>> factory) {
            this.value = value;
            this.factory = factory;
        }

        @NotNull
        public KTN add(TN node) {
            //noinspection unchecked
            return (KTN) this;
        }

        @NotNull
        public TN build(String key) {
            //noinspection unchecked
            return (TN) factory.apply(value);
        }

        public TN build() {
            //noinspection unchecked
            return (TN) factory.apply(value);
        }
    }
}
