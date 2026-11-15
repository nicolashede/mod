package dev.nico.tropiezo;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Lógica del servidor: detecta cuándo te tropezás y te tira al piso. */
public final class TripHandler {
    private static final Map<UUID, Data> DATA = new HashMap<>();

    private TripHandler() {
    }

    private static final class Data {
        boolean init;
        boolean lastOnGround;
        double lastX, lastY, lastZ;
        double airStartY;
        int trippedTicks;
        int cooldown;
        BlockPos lastFlower;
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayerEntity p) {
        TripConfig cfg = TripConfig.get();
        Data d = DATA.computeIfAbsent(p.getUuid(), k -> new Data());

        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();
        boolean onGround = p.isOnGround();

        if (!d.init) {
            d.init = true;
            d.airStartY = y;
            save(d, x, y, z, onGround);
            return;
        }

        double dx = x - d.lastX;
        double dy = y - d.lastY;
        double dz = z - d.lastZ;
        double speed = Math.sqrt(dx * dx + dz * dz);

        // Desde dónde empezó la caída
        if (d.lastOnGround && !onGround) {
            d.airStartY = d.lastY;
        }

        if (!p.isAlive()) {
            endTrip(p, d);
        } else if (d.trippedTicks > 0) {
            d.trippedTicks--;
            if (d.trippedTicks == 0) {
                endTrip(p, d);
            }
        } else {
            if (d.cooldown > 0) {
                d.cooldown--;
            }
            if (cfg.enabled && d.cooldown == 0 && eligible(p, cfg) && shouldTrip(p, d, cfg, dy, speed, onGround)) {
                startTrip(p, d, cfg, dx, dz);
            }
        }

        save(d, x, y, z, onGround);
    }

    private static void save(Data d, double x, double y, double z, boolean onGround) {
        d.lastX = x;
        d.lastY = y;
        d.lastZ = z;
        d.lastOnGround = onGround;
    }

    private static boolean eligible(ServerPlayerEntity p, TripConfig cfg) {
        if (p.isSpectator() || p.hasVehicle() || p.isTouchingWater() || p.isInLava()
                || p.isClimbing() || p.isFallFlying() || p.isSleeping() || p.getAbilities().flying) {
            return false;
        }
        if (!cfg.tripInCreative && p.isCreative()) {
            return false;
        }
        return !cfg.requireSprint || p.isSprinting();
    }

    private static boolean shouldTrip(ServerPlayerEntity p, Data d, TripConfig cfg, double dy, double speed, boolean onGround) {
        // Flores: tener el bloque de los pies dentro de una flor (no tienen colisión)
        BlockPos feet = p.getBlockPos();
        boolean inFlower = p.getWorld().getBlockState(feet).isIn(BlockTags.FLOWERS);
        if (!inFlower) {
            d.lastFlower = null;
        }

        if (speed < cfg.minSpeed) {
            return false;
        }

        // 1) Subir un desnivel chico (1 píxel de tierra arada/camino, cama, alfombra...)
        if (cfg.tripOnStepUp && d.lastOnGround && onGround && dy > 0.02 && dy < 0.65) {
            if (cfg.tripOnStairsAndSlabs || !standingOnStairsOrSlab(p)) {
                if (roll(p, cfg.stepUpChance)) {
                    return true;
                }
            }
        }

        // 2) Flores (una sola tirada por flor)
        if (cfg.tripOnFlowers && inFlower && !feet.equals(d.lastFlower)) {
            d.lastFlower = feet.toImmutable();
            if (roll(p, cfg.flowerChance)) {
                return true;
            }
        }

        // 3) Caerse de un bloque: aterrizar corriendo después de bajar al menos minFallHeight
        if (cfg.tripOnFall && !d.lastOnGround && onGround && (d.airStartY - p.getY()) >= cfg.minFallHeight) {
            return roll(p, cfg.fallChance);
        }

        return false;
    }

    private static boolean standingOnStairsOrSlab(ServerPlayerEntity p) {
        Box box = p.getBoundingBox();
        int y = MathHelper.floor(box.minY - 0.01);
        for (int bx = MathHelper.floor(box.minX); bx <= MathHelper.floor(box.maxX); bx++) {
            for (int bz = MathHelper.floor(box.minZ); bz <= MathHelper.floor(box.maxZ); bz++) {
                BlockState state = p.getWorld().getBlockState(new BlockPos(bx, y, bz));
                if (state.isIn(BlockTags.STAIRS) || state.isIn(BlockTags.SLABS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean roll(ServerPlayerEntity p, double chance) {
        return chance >= 1.0 || p.getRandom().nextDouble() < chance;
    }

    /** Para el comando /tropiezo: tira al jugador sin importar las condiciones. */
    public static void forceTrip(ServerPlayerEntity p) {
        Data d = DATA.computeIfAbsent(p.getUuid(), k -> new Data());
        if (d.trippedTicks > 0) {
            return;
        }
        startTrip(p, d, TripConfig.get(), 0, 0);
    }

    private static void startTrip(ServerPlayerEntity p, Data d, TripConfig cfg, double dx, double dz) {
        d.trippedTicks = cfg.tripDurationTicks;
        d.cooldown = cfg.cooldownTicks;
        TripState.SERVER.add(p.getId());

        Vec3d dir = new Vec3d(dx, 0, dz);
        if (dir.lengthSquared() < 1.0E-6) {
            dir = Vec3d.fromPolar(0, p.getYaw());
        }
        dir = dir.normalize();

        p.setSprinting(false);
        p.setVelocity(dir.x * cfg.pushStrength, cfg.hopStrength, dir.z * cfg.pushStrength);
        p.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));

        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_PLAYER_BIG_FALL, SoundCategory.PLAYERS, 0.7F, 1.0F);

        broadcast(p, cfg.tripDurationTicks);
    }

    private static void endTrip(ServerPlayerEntity p, Data d) {
        boolean was = d.trippedTicks > 0 || TripState.SERVER.contains(p.getId());
        d.trippedTicks = 0;
        TripState.SERVER.remove(p.getId());
        if (was) {
            broadcast(p, 0);
        }
    }

    private static void broadcast(ServerPlayerEntity p, int ticks) {
        TripPayload payload = new TripPayload(p.getId(), ticks);
        ServerPlayNetworking.send(p, payload);
        for (ServerPlayerEntity other : PlayerLookup.tracking(p)) {
            ServerPlayNetworking.send(other, payload);
        }
    }

    /** Se llama cuando el jugador se desconecta. */
    public static void forget(ServerPlayerEntity p) {
        DATA.remove(p.getUuid());
        TripState.SERVER.remove(p.getId());
    }
}
