package org.samswi.caperandomizer.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CapeRandomizerCommand {
    static Minecraft client = Minecraft.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandregistryaccess){
        dispatcher.register(literal("caperandomizer")
                .then(literal("set_favorites")
                .executes(commandContext -> {
                    Executors.newScheduledThreadPool(1).schedule(()  -> {
                        client.execute(() -> {
                            client.setScreen(new CapeChoosingScreen(client.screen));
                        });
                    },  1, TimeUnit.MILLISECONDS);
                    return 0;
                })));
    }
}
