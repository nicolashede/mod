package dev.nico.tropiezo;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quién está tirado en el piso ahora mismo (por id de entidad).
 * SERVER lo llena el servidor; CLIENT lo llena el cliente al recibir el paquete.
 * Están separados para que en singleplayer (cliente + servidor en el mismo juego) no se pisen.
 */
public final class TripState {
    public static final Set<Integer> SERVER = ConcurrentHashMap.newKeySet();
    public static final Map<Integer, Integer> CLIENT = new ConcurrentHashMap<>();

    private TripState() {
    }

    public static boolean isTripped(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            return CLIENT.containsKey(player.getId());
        }
        return SERVER.contains(player.getId());
    }
}
