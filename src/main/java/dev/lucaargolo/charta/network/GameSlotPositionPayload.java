package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.GameSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GameSlotPositionPayload(BlockPos pos, int index, float x, float y, float z, float angle)  {
//
//    public static final CustomPacketPayload.Type<GameSlotPositionPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("game_slot_position"));
//
//    public static StreamCodec<ByteBuf, GameSlotPositionPayload> STREAM_CODEC = StreamCodec.composite(
//            BlockPos.STREAM_CODEC,
//            GameSlotPositionPayload::pos,
//            ByteBufCodecs.INT,
//            GameSlotPositionPayload::index,
//            ByteBufCodecs.FLOAT,
//            GameSlotPositionPayload::x,
//            ByteBufCodecs.FLOAT,
//            GameSlotPositionPayload::y,
//            ByteBufCodecs.FLOAT,
//            GameSlotPositionPayload::z,
//            ByteBufCodecs.FLOAT,
//            GameSlotPositionPayload::angle,
//            GameSlotPositionPayload::new
//    );

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> updateGameSlot(this));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateGameSlot(GameSlotPositionPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                GameSlot slot = cardTable.getSlot(payload.index);
                slot.setX(payload.x);
                slot.setY(payload.y);
                slot.setZ(payload.z);
                slot.setAngle(payload.angle);
            });
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(angle);
    }
    public static GameSlotPositionPayload fromBytes(FriendlyByteBuf buf)
    {


        return new GameSlotPositionPayload(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

}
