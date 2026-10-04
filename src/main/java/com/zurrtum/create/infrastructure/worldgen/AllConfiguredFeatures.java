package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllConfiguredFeatures {
    public static final ResourceKey<Feature> ZINC_ORE = register("zinc_ore");
    public static final ResourceKey<Feature> STRIATED_ORES_OVERWORLD = register("striated_ores_overworld");
    public static final ResourceKey<Feature> STRIATED_ORES_NETHER = register("striated_ores_nether");

    public static ResourceKey<Feature> register(String id) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    public static void register() {
    }
}
