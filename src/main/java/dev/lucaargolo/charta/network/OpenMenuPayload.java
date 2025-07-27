//package dev.lucaargolo.charta.network;
//
//import dev.lucaargolo.charta.game.Deck;
//import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.NbtOps;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.protocol.game.ClientGamePacketListener;
//import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.MenuType;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class OpenMenuPayload extends ClientboundOpenScreenPacket {
//    BlockPos pos;
//    Deck deck;
//    int[] players;
//    byte[] options;
//    public OpenMenuPayload(int containerId, MenuType<?> menuType, Component title, BlockPos pos, Deck deck, int[] players, byte[] options) {
//        super(containerId, menuType, title);
//        this.pos = pos;
//        this.deck = deck;
//        this.players = players;
//        this.options = options;
//    }
//
//
//    @Override
//    public void write(FriendlyByteBuf buffer) {
//        buffer.writeVarInt(this.getContainerId());
//        buffer.writeId(BuiltInRegistries.MENU, this.getType());
//        buffer.writeComponent(this.getTitle());
//        buffer.writeBlockPos(pos);
//        buffer.writeNbt((CompoundTag) Deck.CODEC.encodeStart(NbtOps.INSTANCE, deck).result().orElse(null));
//        buffer.writeVarIntArray(players);
//        buffer.writeByteArray(options);
//    }
//}
