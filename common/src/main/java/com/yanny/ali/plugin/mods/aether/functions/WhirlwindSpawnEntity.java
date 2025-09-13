package com.yanny.ali.plugin.mods.aether.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity")
public class WhirlwindSpawnEntity extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private EntityType<?> entityType;
    @FieldAccessor
    private int count;

    public WhirlwindSpawnEntity(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.whirlwind_spawn_entity"));

        tooltip.add(RegistriesTooltipUtils.getEntityTypeTooltip(utils, "ali.property.value.entity_type", entityType));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.count", count));
        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));

        return tooltip;
    }
}
