package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public record CardDecksPayload(LinkedHashMap<ResourceLocation, Deck> cardDecks) {

//    public static final Type<CardDecksPayload> TYPE = new Type<>(Charta.id("card_decks"));
//
//    public static final StreamCodec<ByteBuf, CardDecksPayload> STREAM_CODEC = StreamCodec.composite(
//        ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, Deck.STREAM_CODEC),
//        CardDecksPayload::cardDecks,
//        CardDecksPayload::new
//    );

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Charta.CARD_DECKS.setDecks(cardDecks);
        });
        context.get().setPacketHandled(true);
    }

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeMap(cardDecks, FriendlyByteBuf::writeResourceLocation, (buf3, value)->buf3.writeNbt((CompoundTag) Deck.CODEC.encodeStart(NbtOps.INSTANCE, value).result().orElse(null)) );
    }
    public static CardDecksPayload fromBytes(FriendlyByteBuf buf)
    {
        return new CardDecksPayload(new LinkedHashMap<>(buf.readMap(FriendlyByteBuf::readResourceLocation,(buf3)->Deck.CODEC.parse(NbtOps.INSTANCE, buf3.readNbt()).result().orElse(null))));
    }
}
