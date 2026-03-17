package world.anhgelus.molehunt.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import world.anhgelus.molehunt.Molehunt;

public record GamePayload(boolean gameLaunched) implements CustomPacketPayload {
    public static final Identifier GAME_PACKET_ID = Identifier.fromNamespaceAndPath(Molehunt.MOD_ID, "game");

    public static final CustomPacketPayload.Type<GamePayload> ID = new CustomPacketPayload.Type<>(GAME_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, GamePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GamePayload::gameLaunched,
            GamePayload::new
    );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
