package com.yanny.ali.api;

import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public interface ITooltipNode extends ICoreTooltipNode<IServerUtils> {
    static void encodeNode(IServerUtils utils, ITooltipNode node, FriendlyByteBuf buf) {
        buf.writeResourceLocation(node.getId());
        node.encode(utils, buf);
    }

    static ITooltipNode decodeNode(IClientUtils utils, FriendlyByteBuf buf) {
        ResourceLocation name = buf.readResourceLocation();
        BiFunction<IClientUtils, FriendlyByteBuf, ITooltipNode> factory = utils.getTooltipNodeFactory(name);

        return factory.apply(utils, buf);
    }
}
