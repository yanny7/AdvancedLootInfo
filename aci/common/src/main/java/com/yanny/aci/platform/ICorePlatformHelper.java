package com.yanny.aci.platform;

import com.yanny.aci.api.ICorePlugin;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.nio.file.Path;
import java.util.List;

public interface ICorePlatformHelper<T extends ICorePlugin<?, ?, ?>> {
        List<T> getPlugins();

        Path getConfiguration();

        default Structure.StructureSettings getStructureSettings(Structure structure) {
                return new Structure.StructureSettings(structure.biomes(), structure.spawnOverrides(), structure.step(), structure.terrainAdaptation());
        }
}
