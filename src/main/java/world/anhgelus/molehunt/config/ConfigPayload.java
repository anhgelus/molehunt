package world.anhgelus.molehunt.config;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import world.anhgelus.molehunt.Molehunt;

public record ConfigPayload(boolean showNametags, boolean showSkins, boolean showTab) implements CustomPayload {
    public static final Identifier CONFIG_PACKET_ID = Identifier.of(Molehunt.MOD_ID, "config");

    public static final CustomPayload.Id<ConfigPayload> ID = new CustomPayload.Id<>(CONFIG_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ConfigPayload::showNametags,
            PacketCodecs.BOOL, ConfigPayload::showSkins,
            PacketCodecs.BOOL, ConfigPayload::showTab,
            ConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
