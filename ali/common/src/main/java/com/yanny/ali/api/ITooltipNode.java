package com.yanny.ali.api;

import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ITooltipNode extends ICoreTooltipNode<IServerUtils> {
    static void encodeNode(IServerUtils utils, ITooltipNode node, RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(node.getId());
        node.encode(utils, buf);
    }

    @NotNull
    static ITooltipNode decodeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        ResourceLocation name = buf.readResourceLocation();
        BiFunction<IClientUtils, RegistryFriendlyByteBuf, ITooltipNode> factory = utils.getTooltipNodeFactory(name);

        return factory.apply(utils, buf);
    }
}
