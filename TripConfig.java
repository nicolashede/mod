package dev.nico.tropiezo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuración en config/tropiezo.json (se crea sola la primera vez).
 * Después de editarla, usá /tropiezo reload (permiso 2) o reiniciá el juego.
 */
public final class TripConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TripConfig instance = new TripConfig();

    // General
    public boolean enabled = true;
    public boolean requireSprint = true;      // solo tropieza corriendo (sprint)
    public boolean tripInCreative = true;
    public double minSpeed = 0.15;            // bloques por tick (correr ~0.28, caminar ~0.22)

    // Efecto de la caída
    public int tripDurationTicks = 40;        // 20 ticks = 1 segundo en el piso
    public int cooldownTicks = 40;            // espera después de levantarse
    public double pushStrength = 0.45;        // empujón hacia adelante
    public double hopStrength = 0.20;         // saltito al tropezar

    // Disparadores
    public boolean tripOnStepUp = true;       // subir un desnivel chico (tierra arada, camino, cama, alfombra...)
    public double stepUpChance = 1.0;
    public boolean tripOnStairsAndSlabs = false; // false = escaleras y losas NO te hacen tropezar
    public boolean tripOnFlowers = true;
    public double flowerChance = 0.4;
    public boolean tripOnFall = true;         // caerse de un bloque
    public double fallChance = 1.0;
    public double minFallHeight = 0.9;        // altura mínima de caída (bloques)

    public static TripConfig get() {
        return instance;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("tropiezo.json");
        try {
            if (Files.exists(path)) {
                TripConfig loaded = GSON.fromJson(Files.readString(path), TripConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            }
            // Reescribe el archivo para que siempre tenga todas las opciones nuevas
            Files.writeString(path, GSON.toJson(instance));
        } catch (Exception e) {
            System.err.println("[tropiezo] No se pudo leer/escribir la config: " + e);
        }
    }
}
