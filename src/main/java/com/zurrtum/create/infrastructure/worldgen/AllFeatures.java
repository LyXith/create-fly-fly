package com.zurrtum.create.infrastructure.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFeatures {
    public static final MapCodec<LayeredOreFeature> LAYERED_ORE = register("layered_ore", LayeredOreFeature.CODEC);

    private static <F extends Feature> MapCodec<F> register(String name, MapCodec<F> codec) {
        Registry.register(BuiltInRegistries.FEATURE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name), codec);
        return codec;
    }

    public static void register() {
    }
}
