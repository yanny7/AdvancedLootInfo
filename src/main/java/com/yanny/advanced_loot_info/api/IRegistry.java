package com.yanny.advanced_loot_info.api;

public interface IRegistry {
    <T> void registerFunction(Class<T> clazz, T function);
}
