package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GameSlotResetPayload(BlockPos pos) {

//    public static final Type<GameSlotResetPayload> TYPE = new Type<>(Charta.id("game_slot_reset"));
//
//    public static StreamCodec<ByteBuf, GameSlotResetPayload> STREAM_CODEC = StreamCodec.composite(
//        BlockPos.STREAM_CODEC,
//        GameSlotResetPayload::pos,
//        GameSlotResetPayload::new
//    );

//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }

    public void handleClient(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> resetGameSlots(this));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void resetGameSlots(GameSlotResetPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(CardTableBlockEntity::resetSlots);
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
    public static GameSlotResetPayload fromBytes(FriendlyByteBuf buf)
    {
        return new GameSlotResetPayload(buf.readBlockPos());
    }

}
