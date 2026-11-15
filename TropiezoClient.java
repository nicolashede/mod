package dev.nico.tropiezo.client;

import dev.nico.tropiezo.TripPayload;
import dev.nico.tropiezo.TripState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Iterator;
import java.util.Map;

public class TropiezoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TripPayload.ID, (payload, context) -> {
            if (payload.ticks() <= 0) {
                TripState.CLIENT.remove(payload.entityId());
            } else {
                TripState.CLIENT.put(payload.entityId(), payload.ticks());
            }
        });

        // Cuenta regresiva en el cliente (por si se pierde el paquete de "se levantó")
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Iterator<Map.Entry<Integer, Integer>> it = TripState.CLIENT.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> entry = it.next();
                int left = entry.getValue() - 1;
                if (left <= 0) {
                    it.remove();
                } else {
                    entry.setValue(left);
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TripState.CLIENT.clear());
    }
}
