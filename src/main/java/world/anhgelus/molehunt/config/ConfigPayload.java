package world.anhgelus.molehunt.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import world.anhgelus.molehunt.Molehunt;

public record ConfigPayload(boolean showNametags, boolean showSkins, boolean showTab) implements CustomPacketPayload {
    public static final Identifier CONFIG_PACKET_ID = Identifier.fromNamespaceAndPath(Molehunt.MOD_ID, "config");

    public static final CustomPacketPayload.Type<ConfigPayload> ID = new CustomPacketPayload.Type<>(CONFIG_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ConfigPayload::showNametags,
            ByteBufCodecs.BOOL, ConfigPayload::showSkins,
            ByteBufCodecs.BOOL, ConfigPayload::showTab,
            ConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
