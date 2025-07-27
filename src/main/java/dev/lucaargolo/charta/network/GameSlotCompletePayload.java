package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.GameSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public record GameSlotCompletePayload(BlockPos pos, int index, List<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack, boolean centered)  {

    public GameSlotCompletePayload(BlockPos pos, int index, GameSlot slot) {
        this(pos, index, slot.stream().toList(), slot.getX(), slot.getY(), slot.getZ(), slot.getAngle(), slot.getStackDirection(), slot.getMaxStack(), slot.isCentered());
    }

//    public static final CustomPacketPayload.Type<GameSlotCompletePayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("game_slot_complete"));
//
//    public static StreamCodec<ByteBuf, GameSlotCompletePayload> STREAM_CODEC = ExpandedStreamCodec.composite(
//            BlockPos.STREAM_CODEC,
//            GameSlotCompletePayload::pos,
//            ByteBufCodecs.INT,
//            GameSlotCompletePayload::index,
//            ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC),
//            GameSlotCompletePayload::cards,
//            ByteBufCodecs.FLOAT,
//            GameSlotCompletePayload::x,
//            ByteBufCodecs.FLOAT,
//            GameSlotCompletePayload::y,
//            ByteBufCodecs.FLOAT,
//            GameSlotCompletePayload::z,
//            ByteBufCodecs.FLOAT,
//            GameSlotCompletePayload::angle,
//            Direction.STREAM_CODEC,
//            GameSlotCompletePayload::stackDirection,
//            ByteBufCodecs.FLOAT,
//            GameSlotCompletePayload::maxStack,
//            ByteBufCodecs.BOOL,
//            GameSlotCompletePayload::centered,
//            GameSlotCompletePayload::new
//    );

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }

    public void handleClient(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> updateGameSlot(this));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateGameSlot(GameSlotCompletePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                List<Card> list = new LinkedList<>(payload.cards);
                if(payload.index == cardTable.getSlotCount()) {
                    cardTable.addSlot(new GameSlot(list, payload.x, payload.y, payload.z, payload.angle, payload.stackDirection, payload.maxStack, payload.centered));
                }else{
                    GameSlot tracked = cardTable.getSlot(payload.index);
                    tracked.setCards(list);
                    tracked.setX(payload.x);
                    tracked.setY(payload.y);
                    tracked.setZ(payload.z);
                    tracked.setAngle(payload.angle);
                    tracked.setStackDirection(payload.stackDirection);
                    tracked.setMaxStack(payload.maxStack);
                    tracked.setCentered(payload.centered);
                }
            });
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeInt(cards.size());
        for (Card card : cards)
        {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("card", Card.CODEC.encodeStart(NbtOps.INSTANCE, card).result().orElse(null));
            buf.writeNbt(compoundTag) ;
        }
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(angle);
        buf.writeInt(stackDirection.ordinal());
        buf.writeFloat(maxStack);
        buf.writeBoolean(centered);
    }
    public static GameSlotCompletePayload fromBytes(FriendlyByteBuf buf)
    {
        BlockPos pos1 = buf.readBlockPos();
        int index1 = buf.readInt();
        int cardListSize = buf.readInt();
        List<Card> cardList = new ArrayList<>();
        for (int i = 0;i < cardListSize;i++)
        {
            cardList.add(
                    Card.CODEC.parse(NbtOps.INSTANCE, buf.readNbt().get("card"))
                    .result().orElse(null)
            );
        }

        return new GameSlotCompletePayload(
                pos1,
                index1,
                cardList,
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                Direction.values()[buf.readInt()],
                buf.readFloat(),
                buf.readBoolean()
        );
    }
}
