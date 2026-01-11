package org.samswi.caperandomizer.client.mixin;

import org.samswi.caperandomizer.client.CapeRandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.User;

@Mixin(User.class)
public class SessionMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void loadCapes(String username, UUID uuid, String accessToken, Optional xuid, Optional clientId, CallbackInfo ci){
        new Thread(() -> CapeRandomizerClient.fillCapesAndEquipRandom(accessToken)).start();
    }
}
