package org.samswi.caperandomizer.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.command.CommandRegistryAccess;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CapeRandomizerCommand {
    static MinecraftClient client = MinecraftClient.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandregistryaccess){
        dispatcher.register(literal("caperandomizer")
                .then(literal("set_favorites")
                .executes(commandContext -> {
                    Executors.newScheduledThreadPool(1).schedule(()  -> {
                        client.execute(() -> {
                            client.setScreen(new CapeChoosingScreen());
                        });
                    },  1, TimeUnit.MILLISECONDS);
                    System.out.println("Commaand invoked");
                    return 0;
                })));
    }
}
