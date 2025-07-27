package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GameStartPayload()  {

//    public static final Type<GameStartPayload> TYPE = new Type<>(Charta.id("game_start"));
//
//    public static StreamCodec<ByteBuf, GameStartPayload> STREAM_CODEC = StreamCodec.unit(new GameStartPayload());

    public  void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(GameStartPayload::onGameStart);
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onGameStart() {
        ChartaClient.LOCAL_HISTORY.clear();
    }

//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
    }
    public static GameStartPayload fromBytes(FriendlyByteBuf buf)
    {
        return new GameStartPayload();
    }
}
