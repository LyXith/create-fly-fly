package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Mixin(RecipeMap.class)
public class RecipeMapMixin {
    /**
     * Recipes are a data registry; the ones Create derives at load time (sequenced assembly steps, potion mixing)
     * are added when the server's recipe map is built from that registry.
     */
    @Inject(method = "create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;", remap = false))
    private static void addGeneratedRecipes(
        HolderLookup<Recipe<?>> lookup,
        CallbackInfoReturnable<RecipeMap> cir,
        @Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType,
        @Local ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey
    ) {
        Map<Identifier, Recipe<?>> generated = new TreeMap<>(SequencedAssemblyRecipe.GENERATE_RECIPES);
        PotionRecipe.register(lookup, generated);
        Set<ResourceKey<Recipe<?>>> existing = lookup.listElementIds().collect(Collectors.toSet());
        generated.forEach((id, recipe) -> {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
            if (existing.contains(key)) {
                return;
            }
            RecipeHolder<?> holder = new RecipeHolder<>(key, recipe);
            byType.put(recipe.getType(), holder);
            byKey.put(key, holder);
        });
    }
}
