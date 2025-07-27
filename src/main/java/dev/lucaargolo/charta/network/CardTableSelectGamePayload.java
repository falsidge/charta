package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CardTableSelectGamePayload(BlockPos pos, ResourceLocation gameId, byte[] options) {

//    public static final Type<CardTableSelectGamePayload> TYPE = new Type<>(Charta.id("card_table_select_game"));
//
//    public static final StreamCodec<ByteBuf, CardTableSelectGamePayload> STREAM_CODEC = StreamCodec.composite(
//            BlockPos.STREAM_CODEC,
//            CardTableSelectGamePayload::pos,
//            ResourceLocation.STREAM_CODEC,
//            CardTableSelectGamePayload::gameId,
//            ByteBufCodecs.BYTE_ARRAY,
//            CardTableSelectGamePayload::options,
//            CardTableSelectGamePayload::new
//    );

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> context.get().getSender().level().getBlockEntity(pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(table -> {
            if(table.getGame() == null || table.getGame().isGameOver()) {
                Component result = table.startGame(gameId, options);
                context.get().getSender().displayClientMessage(result, true);
            }
        }));
        context.get().setPacketHandled(true);
    }

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(gameId);
        buf.writeByteArray(options);
    }
    public static CardTableSelectGamePayload fromBytes(FriendlyByteBuf buf)
    {
        return new CardTableSelectGamePayload(buf.readBlockPos(), buf.readResourceLocation(),buf.readByteArray());
    }
}
