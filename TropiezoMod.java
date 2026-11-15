package dev.nico.tropiezo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TropiezoMod implements ModInitializer {
    public static final String MOD_ID = "tropiezo";

    @Override
    public void onInitialize() {
        TripConfig.load();

        PayloadTypeRegistry.playS2C().register(TripPayload.ID, TripPayload.CODEC);

        ServerTickEvents.END_SERVER_TICK.register(TripHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TripHandler.forget(handler.getPlayer()));

        // /tropiezo        -> te tropezás vos (sirve para probar)
        // /tropiezo reload -> recarga config/tropiezo.json (requiere permiso 2)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("tropiezo")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            TripHandler.forceTrip(player);
                            return 1;
                        })
                        .then(CommandManager.literal("reload")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(ctx -> {
                                    TripConfig.load();
                                    ctx.getSource().sendFeedback(() -> Text.literal("Config de Tropiezo recargada"), false);
                                    return 1;
                                }))));
    }
}
