package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record PlayerOptionsPayload(HashMap<ResourceLocation, byte[]> playerOptions)  {

//    public static final Type<PlayerOptionsPayload> TYPE = new Type<>(Charta.id("player_options"));
//
//    public static StreamCodec<ByteBuf, PlayerOptionsPayload> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BYTE_ARRAY),
//            PlayerOptionsPayload::playerOptions,
//            PlayerOptionsPayload::new
//    );

    public void handleBoth(Supplier<NetworkEvent.Context> context){
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            handleServer(context);
        }else{
            handleClient(context);
        }
        context.get().setPacketHandled(true);
    }

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ChartaClient.LOCAL_OPTIONS.clear();
            ChartaClient.LOCAL_OPTIONS.putAll(playerOptions);
        });
    }

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
//            if(context.get().getSender() instanceof ServerPlayer serverPlayer) {
            ServerPlayer serverPlayer = context.get().getSender();;
            PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData::load, PlayerOptionData::new,"charta_player_options");
            data.setPlayerOptions(serverPlayer, playerOptions());
//            }
        });
    }

//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeMap(playerOptions, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeByteArray);
    }
    public static PlayerOptionsPayload fromBytes(FriendlyByteBuf buf)
    {
        return new PlayerOptionsPayload((HashMap<ResourceLocation, byte[]>) buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readByteArray));
    }
}
