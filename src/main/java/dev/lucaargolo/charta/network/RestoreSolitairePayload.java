package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RestoreSolitairePayload() {
//
//    public static final Type<RestoreSolitairePayload> TYPE = new Type<>(Charta.id("restore_solitaire"));
//
//    public static final StreamCodec<ByteBuf, RestoreSolitairePayload> STREAM_CODEC = StreamCodec.unit(new RestoreSolitairePayload());

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            restore(context);
        });
        context.get().setPacketHandled(true);
    }
    public void restore(Supplier<NetworkEvent.Context> context)
    {
        Player player = context.get().getSender();
        if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof SolitaireMenu solitaireMenu) {
            solitaireMenu.getGame().restore();
        }
    }
//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
    }
    public static RestoreSolitairePayload fromBytes(FriendlyByteBuf buf)
    {
        return new RestoreSolitairePayload();
    }
}
