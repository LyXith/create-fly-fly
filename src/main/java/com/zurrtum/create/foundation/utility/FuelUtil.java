package com.zurrtum.create.foundation.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Optional;

/**
 * Burn times are an item component whose value is resolved through a loot context.
 * This is the context-free lookup the old Level#fuelValues() gave (a plain furnace, no block bonus).
 */
public class FuelUtil {
    /**
     * Burn time reported on the logical client, where the context providers behind the component cannot be resolved.
     */
    private static final int CLIENT_FALLBACK = 1600;

    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL);
    }

    public static int burnDuration(Level level, ItemStack stack) {
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) {
            return 0;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return CLIENT_FALLBACK;
        }
        LootContext context = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)).create(
            Optional.empty());
        return fuel.burnTime().get(context, 0);
    }
}
