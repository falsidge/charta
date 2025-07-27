package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record UpdateCardContainerSlotPayload(int containerId, int stateId, int slotId, List<Card> cards)  {

//    public static final CustomPacketPayload.Type<UpdateCardContainerSlotPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("update_card_container_slot"));
//
//    public static final StreamCodec<ByteBuf, UpdateCardContainerSlotPayload> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.INT,
//            UpdateCardContainerSlotPayload::containerId,
//            ByteBufCodecs.INT,
//            UpdateCardContainerSlotPayload::stateId,
//            ByteBufCodecs.INT,
//            UpdateCardContainerSlotPayload::slotId,
//            ByteBufCodecs.collection(NonNullList::createWithCapacity, Card.STREAM_CODEC),
//            UpdateCardContainerSlotPayload::cards,
//            UpdateCardContainerSlotPayload::new
//    );

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            setCards(context);
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void setCards(Supplier<NetworkEvent.Context> context)
    {
        Player player = Minecraft.getInstance().player;
        if(player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == containerId) {
            cardMenu.setCards(slotId, stateId, cards);
        }
    }
//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
public void toBytes(FriendlyByteBuf buf) {
    buf.writeInt(containerId);
    buf.writeInt(stateId);
    buf.writeInt(slotId);
    buf.writeInt(cards.size());
    for (Card card : cards)
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("card", Card.CODEC.encodeStart(NbtOps.INSTANCE, card).result().orElse(null));
        buf.writeNbt(compoundTag) ;
    }
}
public static UpdateCardContainerSlotPayload fromBytes(FriendlyByteBuf buf)
    {
        int containerId2 = buf.readInt();
        int stateId2 = buf.readInt();
        int slotId2 = buf.readInt();
        int cardListSize = buf.readInt();
        List<Card> cardList = new ArrayList<>();
        for (int i = 0;i < cardListSize;i++)
        {
            cardList.add(
                    Card.CODEC.parse(NbtOps.INSTANCE, buf.readNbt().get("card"))
                            .result().orElse(null)
            );
        }
        return new UpdateCardContainerSlotPayload(
                containerId2,
                stateId2,
                slotId2,
                cardList
        );
    }
}
