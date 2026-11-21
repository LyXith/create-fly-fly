package com.zurrtum.create.mixin;

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTransformer.class)
public class BlockTransformerMixin {
    @Inject(method = "playerHasBlockingItemUseIntent(Lnet/minecraft/world/item/context/UseOnContext;)Z", at = @At("HEAD"), cancellable = true)
    private static void allowPlayerlessToolUse(UseOnContext context, CallbackInfoReturnable<Boolean> cir) {
        if (context.getPlayer() == null) {
            cir.setReturnValue(false);
        }
    }
}
