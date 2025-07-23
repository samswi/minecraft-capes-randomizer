package org.samswi.caperandomizer.client.mixin;

import net.minecraft.client.session.Session;
import org.samswi.caperandomizer.Caperandomizer;
import org.samswi.caperandomizer.client.CaperandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Session.class)
public class SessionMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void loadCapes(String username, UUID uuid, String accessToken, Optional xuid, Optional clientId, Session.AccountType accountType, CallbackInfo ci){
        new Thread(() -> CaperandomizerClient.fillCapesList(accessToken)).start();
        new Thread(CaperandomizerClient::equipRandomCape).start();
    }
}
