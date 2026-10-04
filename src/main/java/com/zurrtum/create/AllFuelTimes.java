package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

import static com.zurrtum.create.Create.MOD_ID;

/**
 * Burn times live in data/create/context_int_provider/cooking and are attached to the items as cooking fuel components.
 */
public class AllFuelTimes {
    public static final ResourceKey<ContextIntProvider> BLAZE_CAKE = key("time_blaze_cake");
    public static final ResourceKey<ContextIntProvider> CREATIVE_BLAZE_CAKE = key("time_creative_blaze_cake");
    public static final ResourceKey<ContextIntProvider> CARDBOARD = key("time_cardboard");
    public static final ResourceKey<ContextIntProvider> CARDBOARD_BLOCK = key("time_cardboard_block");

    private static ResourceKey<ContextIntProvider> key(String name) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Identifier.fromNamespaceAndPath(MOD_ID, "cooking/" + name));
    }

    public static void register() {
    }
}
