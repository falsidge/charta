package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.fun.FunScreen;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LastFunPayload(ItemStack deckStack) {

    public LastFunPayload() {
        this(ItemStack.EMPTY);
    }

//    public static final Type<LastFunPayload> TYPE = new Type<>(Charta.id("last_fun"));
//
//    private static final StreamCodec<ByteBuf, ItemStack> STACK_STREAM = ByteBufCodecs.fromCodecTrusted(ItemStack.OPTIONAL_CODEC);
//    public static StreamCodec<ByteBuf, LastFunPayload> STREAM_CODEC = StreamCodec.composite(
//            STACK_STREAM,
//            LastFunPayload::deckStack,
//            LastFunPayload::new
//    );

    public void handleBoth(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            handleServer(context);
        }else{
            handleClient(context);
        }
        context.get().setPacketHandled(true);
    }

    public void handleServer(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender();
            if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof FunMenu funMenu) {
                funMenu.getGame().sayLast(((LivingEntityMixed) player).charta_getCardPlayer());
            }
        });
    }

    public void handleClient(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> displayTotemEffect(deckStack));
    }

    @OnlyIn(Dist.CLIENT)
    public static void displayTotemEffect(ItemStack deckStack) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && mc.screen instanceof FunScreen funScreen) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.TOTEM_USE, mc.player.getSoundSource(), 1.0F, 1.0F, false);
            funScreen.displayItemActivation(deckStack);
        }
    }

//    @Override
//    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
    public void toBytes(FriendlyByteBuf buf) {
            buf.writeItemStack(deckStack, false);
    }
    public static LastFunPayload fromBytes(FriendlyByteBuf buf)
    {
        return new LastFunPayload(buf.readItem());
    }
}
