package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.configs.forge.RSModdedLootConfig;
import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.StructureModdedLootImporterApplier;
import com.telepathicgrunt.repurposedstructures.misc.lootmanager.StructureModdedLootImporter;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StructureModdedLootImporterApplierAccessor extends BaseAccessor<StructureModdedLootImporterApplier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public StructureModdedLootImporterApplierAccessor(StructureModdedLootImporterApplier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(conditions), (c) -> getOperations(utils, c));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table(StructureModdedLootImporterApplierAccessor::isImported, false);
    }

    @NotNull
    private static List<IOperation> getOperations(IServerUtils utils, List<LootItemCondition> conditions) {
        ResourceLocation location = TooltipContext.get();
        ResourceLocation imported = location == null ? null : StructureModdedLootImporter.TABLE_IMPORTS.get(location);

        if (imported == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getImportNode(utils, conditions, imported)));
    }

    @NotNull
    private static IDataNode getImportNode(IServerUtils utils, List<LootItemCondition> conditions, ResourceLocation imported) {
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(TooltipBuilder.keyOnly(RepurposedStructuresLang.Value.MODDED_ITEMS_ONLY));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
        }).build();

        return NodeUtils.getReferenceNode(utils, imported, conditions, tooltip);
    }

    private static boolean isImported(ResourceLocation location) {
        return RSModdedLootConfig.importModdedItems.get() && StructureModdedLootImporter.TABLE_IMPORTS.containsKey(location) && !StructureModdedLootImporter.isInBlacklist(location);
    }
}
