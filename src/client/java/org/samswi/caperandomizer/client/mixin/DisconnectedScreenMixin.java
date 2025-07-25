package org.samswi.caperandomizer.client.mixin;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.samswi.caperandomizer.client.CaperandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/network/DisconnectionInfo;Lnet/minecraft/text/Text;)V")
    private static void equipRandomCape(Screen parent, Text title, DisconnectionInfo info, Text buttonLabel, CallbackInfo ci){
        new Thread(CaperandomizerClient::equipRandomCape).start();
    }
}
