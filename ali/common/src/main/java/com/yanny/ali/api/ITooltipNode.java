package com.yanny.ali.api;

import com.yanny.aci.api.ICommonTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.function.BiFunction;

public interface ITooltipNode extends ICommonTooltipNode<IServerUtils> {
    static void encodeNode(IServerUtils utils, ITooltipNode node, RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(node.getId());
        node.encode(utils, buf);
    }

    static ITooltipNode decodeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        Identifier name = buf.readIdentifier();
        BiFunction<IClientUtils, RegistryFriendlyByteBuf, ITooltipNode> factory = utils.getTooltipNodeFactory(name);

        if (factory != null) {
            return factory.apply(utils, buf);
        } else {
            throw new IllegalStateException("No factory defined for tooltip node " + name);
        }
    }
}
