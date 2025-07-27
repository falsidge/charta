package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.ConfirmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GameLeavePayload() {

//    public static final Type<GameLeavePayload> TYPE = new Type<>(Charta.id("game_leave"));
//
//    public static StreamCodec<ByteBuf, GameLeavePayload> STREAM_CODEC = StreamCodec.unit(new GameLeavePayload());
//
    public void handleBoth(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            handleServer(context);
        }else{
            handleClient(context);
        }
        context.get().setPacketHandled(true);
    }

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            context.get().getSender().stopRiding();
        });
    }

    public static void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(GameLeavePayload::openExitScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openExitScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(null, Component.translatable("message.charta.leaving_game"), true, () -> {
            if(minecraft.player != null) {
                minecraft.player.stopRiding();
                Charta.INSTANCE.sendToServer(new GameLeavePayload());
            }
        }));
    }

//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
    }
    public static GameLeavePayload fromBytes(FriendlyByteBuf buf)
    {
        return new GameLeavePayload();
    }
}
