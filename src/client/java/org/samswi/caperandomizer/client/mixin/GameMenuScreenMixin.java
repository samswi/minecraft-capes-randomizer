package org.samswi.caperandomizer.client.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import org.samswi.caperandomizer.client.CapeRandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {

    @Inject(at = @At("TAIL"), method = "disconnect")
    private static void equipRandomCape(CallbackInfo ci){
        new Thread(CapeRandomizerClient::equipRandomCape).start();
    }

}
