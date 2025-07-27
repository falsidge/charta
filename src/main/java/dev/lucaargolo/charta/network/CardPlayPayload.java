package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.client.gui.screens.HistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.function.Supplier;

public record CardPlayPayload(Component playerName, int playerCards, Component play) {
//
//    public static final CustomPacketPayload.Type<CardPlayPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("card_play"));
//
//    public static StreamCodec<ByteBuf, CardPlayPayload> STREAM_CODEC = StreamCodec.composite(
//        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
//        CardPlayPayload::playerName,
//        ByteBufCodecs.INT,
//        CardPlayPayload::playerCards,
//        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
//        CardPlayPayload::play,
//        CardPlayPayload::new
//    );

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            addToHistory(playerName, playerCards, play);
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void addToHistory(Component playerName, int playerCards, Component play) {
        ChartaClient.LOCAL_HISTORY.add(ImmutableTriple.of(playerName, playerCards, play));
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen instanceof HistoryScreen screen) {
            screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }
    }

//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeComponent(playerName);
        buf.writeInt(playerCards);
        buf.writeComponent(play);
    }
    public static CardPlayPayload fromBytes(FriendlyByteBuf buf)
    {
        return new CardPlayPayload(buf.readComponent(), buf.readInt(), buf.readComponent());
    }
}
