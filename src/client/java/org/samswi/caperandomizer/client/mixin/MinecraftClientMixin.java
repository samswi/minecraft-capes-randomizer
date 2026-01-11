package org.samswi.caperandomizer.client.mixin;

import org.samswi.caperandomizer.client.CapeRandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Mixin(Minecraft.class)
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

    @Inject(at = @At("HEAD"), method = "emergencySave")
    private void resetCapeOnCrash(CallbackInfo ci){
        try {
            CapeRandomizerClient.resetCape();
        } catch (Exception e) {
            CapeRandomizerClient.LOGGER.error("Could not reset cape");
        }
    }

    @Inject(at = @At("TAIL"), method = "disconnectFromWorld(Lnet/minecraft/network/chat/Component;)V")
    private void equipRandomCape(Component reasonText, CallbackInfo ci){
        new Thread(CapeRandomizerClient::equipRandomCape).start();
    }
}
