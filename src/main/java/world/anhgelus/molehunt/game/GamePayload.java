package world.anhgelus.molehunt.game;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import world.anhgelus.molehunt.Molehunt;

public record GamePayload(boolean gameLaunched) implements CustomPayload {
    public static final Identifier GAME_PACKET_ID = Identifier.of(Molehunt.MOD_ID, "game");

    public static final CustomPayload.Id<GamePayload> ID = new CustomPayload.Id<>(GAME_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, GamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, GamePayload::gameLaunched,
            GamePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
