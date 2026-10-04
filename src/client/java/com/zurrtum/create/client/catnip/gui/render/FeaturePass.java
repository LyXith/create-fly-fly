package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;

/**
 * Feature rendering takes the render pass to draw into (there is no global output override any more).
 * This opens a pass on an off-screen colour/depth pair and runs the submitted features in it.
 */
public final class FeaturePass {
    private FeaturePass() {
    }

    public static void renderAllFeatures(
        FeatureRenderDispatcher dispatcher,
        SubmitNodeStorage storage,
        GpuTextureView color,
        GpuTextureView depth
    ) {
        render(dispatcher, storage, color, depth, FeatureRenderDispatcher::renderAllFeatures);
    }

    public static void render(
        FeatureRenderDispatcher dispatcher,
        SubmitNodeStorage storage,
        GpuTextureView color,
        GpuTextureView depth,
        BiConsumer<RenderPass, FeatureRenderDispatcher.PreparedFrame> action
    ) {
        try (FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage); RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "Create off-screen features", color, Optional.empty(), depth, OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            action.accept(pass, frame);
        }
    }
}
