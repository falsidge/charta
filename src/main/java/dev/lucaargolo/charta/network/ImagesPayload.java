package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.SuitImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

public record ImagesPayload(HashMap<ResourceLocation, SuitImage> suitImages, HashMap<ResourceLocation, CardImage> cardImages, HashMap<ResourceLocation, CardImage> deckImages) {

//    public static final CustomPacketPayload.Type<ImagesPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("card_images"));
//
//    public static final StreamCodec<ByteBuf, ImagesPayload> STREAM_CODEC = StreamCodec.composite(
//        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.SUIT_STREAM_CODEC),
//        ImagesPayload::suitImages,
//        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
//        ImagesPayload::cardImages,
//        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
//        ImagesPayload::deckImages,
//        ImagesPayload::new
//    );

    public void handleClient(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ChartaClient.clearImages();
            Charta.SUIT_IMAGES.setImages(suitImages());
            Charta.CARD_IMAGES.setImages(cardImages());
            Charta.DECK_IMAGES.setImages(deckImages());
            ChartaClient.generateImages();
        });
        context.get().setPacketHandled(true);
    }

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
            buf.writeMap(suitImages, FriendlyByteBuf::writeResourceLocation,(buf1, suitImage)->buf1.writeByteArray(suitImage.compress()));
            buf.writeMap(cardImages, FriendlyByteBuf::writeResourceLocation,(buf1, cardImage)->buf1.writeByteArray(cardImage.compress()));
            buf.writeMap(deckImages, FriendlyByteBuf::writeResourceLocation,(buf1, cardImage)->buf1.writeByteArray(cardImage.compress()));
    }
    public static ImagesPayload fromBytes(FriendlyByteBuf buf)
    {
        return new ImagesPayload(
                new HashMap<> (buf.readMap(FriendlyByteBuf::readResourceLocation,(buf1)->SuitImage.decompress(buf1.readByteArray()))),
                new HashMap<> (buf.readMap(FriendlyByteBuf::readResourceLocation,(buf1)->CardImage.decompress(buf1.readByteArray()))),
                new HashMap<> (buf.readMap(FriendlyByteBuf::readResourceLocation,(buf1)->CardImage.decompress(buf1.readByteArray())))
        );
    }
}
