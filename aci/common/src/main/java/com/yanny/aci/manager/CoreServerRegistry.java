package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CoreServerRegistry implements ICoreServerUtils {
    private final List<ManagedRegistry<?, ?>> allRegistries = new ArrayList<>();
    private ServerLevel serverLevel;

    public void clearData() {
        allRegistries.forEach(ManagedRegistry::clear);
    }

    public void printRegistrationInfo() {
        allRegistries.forEach(ManagedRegistry::logStatistics);
    }

    public void printRuntimeInfo() {
        allRegistries.forEach(ManagedRegistry::logMissing);
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Nullable
    @Override
    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    @NotNull
    protected <V> ManagedRegistry<Class<?>, V> registerClassKeyed(String label, boolean reportMissing, Supplier<Map<Class<?>, V>> mapSupplier, @Nullable Registry<?> registry) {
        return register(label, reportMissing, mapSupplier, Class::getTypeName, registry);
    }

    @NotNull
    protected <K, V> ManagedRegistry<K, V> register(String label, boolean reportMissing, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyNameGetter, @Nullable Registry<?> registry) {
        ManagedRegistry<K, V> reg = new ManagedRegistry<>(label, reportMissing, mapSupplier, keyNameGetter, registry);
        allRegistries.add(reg);
        return reg;
    }
}
