package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record CardContainerSlotClickPayload(int containerId, int slotId, int cardId)  {

//    public static final Type<CardContainerSlotClickPayload> TYPE = new Type<>(Charta.id("card_container_slot_click"));
//
//    public static final StreamCodec<ByteBuf, CardContainerSlotClickPayload> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.INT,
//            CardContainerSlotClickPayload::containerId,
//            ByteBufCodecs.INT,
//            CardContainerSlotClickPayload::slotId,
//            ByteBufCodecs.INT,
//            CardContainerSlotClickPayload::cardId,
//            CardContainerSlotClickPayload::new
//    );

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender();
            if(player instanceof LivingEntityMixed mixed && player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == containerId) {
                CardPlayer cardPlayer = mixed.charta_getCardPlayer();
                CardSlot<?> slot = cardMenu.getCardSlot(slotId);
                GameSlot carriedCards = cardMenu.getCarriedCards();
                if(carriedCards.isEmpty() && slot.canRemoveCard(cardPlayer, cardId)) {
                    slot.preUpdate();
                    List<Card> cards = slot.removeCards(cardId);
                    cardMenu.setCarriedCards(new GameSlot(cards));
                    slot.onRemove(cardPlayer, cards);
                    slot.postUpdate();
                }else if(!carriedCards.isEmpty() && slot.canInsertCard(cardPlayer, carriedCards.stream().toList(), cardId) && slot.insertCards(carriedCards, cardId)) {
                    slot.preUpdate();
                    cardMenu.setCarriedCards(new GameSlot());
                    slot.onInsert(cardPlayer, carriedCards.stream().toList());
                    slot.postUpdate();
                }
            }
        });
        context.get().setPacketHandled(true);
    }

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(containerId);
            buf.writeInt(slotId);
            buf.writeInt(cardId);
        }
        public static CardContainerSlotClickPayload fromBytes(FriendlyByteBuf buf)
        {
            return new CardContainerSlotClickPayload(buf.readInt(), buf.readInt(), buf.readInt());
        }
}
