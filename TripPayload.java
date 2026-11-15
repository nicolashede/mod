package dev.nico.tropiezo;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> cliente: "este jugador se cayó por N ticks" (ticks = 0 significa que se levantó). */
public record TripPayload(int entityId, int ticks) implements CustomPayload {
    public static final CustomPayload.Id<TripPayload> ID =
            new CustomPayload.Id<>(Identifier.of(TropiezoMod.MOD_ID, "trip"));

    public static final PacketCodec<RegistryByteBuf, TripPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, TripPayload::entityId,
            PacketCodecs.VAR_INT, TripPayload::ticks,
            TripPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
