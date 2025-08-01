package org.samswi.caperandomizer.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.samswi.caperandomizer.client.CapeRandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "close")
    private static void resetCapeToOriginalState(CallbackInfo ci){
        try {
            CapeRandomizerClient.resetCape();
        } catch (IOException | InterruptedException e) {
            CapeRandomizerClient.LOGGER.error("Failed to reset cape to its original form");
            throw new RuntimeException(e);
        }
    }

    @Inject(at = @At("HEAD"), method = "cleanUpAfterCrash")
    private void resetCapeOnCrash(CallbackInfo ci){
        try {
            CapeRandomizerClient.resetCape();
        } catch (Exception e) {
            CapeRandomizerClient.LOGGER.error("Could not reset cape");
        }
    }
}
