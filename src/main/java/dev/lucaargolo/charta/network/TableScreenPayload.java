package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.gui.screens.TableScreen;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TableScreenPayload(BlockPos pos, Deck deck, int[] players) {

//    public static final Type<TableScreenPayload> TYPE = new Type<>(Charta.id("table_screen"));
//
//    public static final StreamCodec<ByteBuf, TableScreenPayload> STREAM_CODEC = StreamCodec.composite(
//            BlockPos.STREAM_CODEC,
//            TableScreenPayload::pos,
//            Deck.STREAM_CODEC,
//            TableScreenPayload::deck,
//            new StreamCodec<>() {
//                @Override
//                public void encode(@NotNull ByteBuf buffer, int @NotNull [] value) {
//                    buffer.writeInt(value.length);
//                    for (int i : value) {
//                        buffer.writeInt(i);
//                    }
//                }
//
//                @Override
//                public int @NotNull [] decode(@NotNull ByteBuf buffer) {
//                    int length = buffer.readInt();
//                    int[] array = new int[length];
//                    for(int i = 0; i < length; i++) {
//                        array[i] = buffer.readInt();
//                    }
//                    return array;
//                }
//            },
//            TableScreenPayload::players,
//            TableScreenPayload::new
//    );

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            openScreen(pos, deck, players);
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(BlockPos pos, Deck deck, int[] players) {
        Minecraft.getInstance().setScreen(new TableScreen(pos, deck, players));
    }
//
//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(Deck.CODEC.encodeStart(NbtOps.INSTANCE, deck).result().map((compoundTag)-> (CompoundTag) compoundTag).orElse(null));
        buf.writeVarIntArray(players);
    }
    public static TableScreenPayload fromBytes(FriendlyByteBuf buf)
    {
        return new TableScreenPayload(buf.readBlockPos(),
                Deck.CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(null),
                buf.readVarIntArray()
            );
    }
}
