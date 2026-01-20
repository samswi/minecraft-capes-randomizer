package org.samswi.caperandomizer.client.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerSkin;
import org.samswi.caperandomizer.client.CapeRandomizerClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.samswi.caperandomizer.client.CapeRandomizerClient.LOGGER;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
    @Unique
    Gson gson = new Gson();

    @Inject(method = "get", at = @At(value = "INVOKE", target = "Lcom/google/common/cache/LoadingCache;getUnchecked(Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    void injectCape(GameProfile gameProfile, CallbackInfoReturnable<CompletableFuture<Optional<PlayerSkin>>> cir, @Local Property property){
        if (gameProfile.id().equals(Minecraft.getInstance().getGameProfile().id()) && Minecraft.getInstance().isSingleplayer() && CapeRandomizerClient.currentCape != null){
            try {
                String decodedString = new String(Base64.getDecoder().decode(property.value().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                JsonObject jsonObject = gson.fromJson(decodedString, JsonObject.class);
                jsonObject.getAsJsonObject("textures")
                        .getAsJsonObject("CAPE")
                        .addProperty("url", CapeRandomizerClient.currentCape.url);
                String encodedNewString = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                Property newProperty = new Property("textures", encodedNewString);
                cir.setReturnValue(((SkinManager)(Object)this).skinCache.getUnchecked(new SkinManager.CacheKey(gameProfile.id(), newProperty)));
                cir.cancel();
            } catch (Exception e) {
                LOGGER.error("Something went wrong when injecting cape for singleplayer: {}", e.getMessage());
            }
        }
    }
}
