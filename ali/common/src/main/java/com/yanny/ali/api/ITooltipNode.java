package com.yanny.ali.api;

import com.yanny.aci.api.ICommonTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public interface ITooltipNode extends ICommonTooltipNode<IServerUtils> {
    static void encodeNode(IServerUtils utils, ITooltipNode node, FriendlyByteBuf buf) {
        buf.writeResourceLocation(node.getId());
        node.encode(utils, buf);
    }

    static ITooltipNode decodeNode(IClientUtils utils, FriendlyByteBuf buf) {
        ResourceLocation name = buf.readResourceLocation();
        BiFunction<IClientUtils, FriendlyByteBuf, ITooltipNode> factory = utils.getTooltipNodeFactory(name);

        if (factory != null) {
            return factory.apply(utils, buf);
        } else {
            throw new IllegalStateException("No factory defined for tooltip node " + name);
        }
    }
}
