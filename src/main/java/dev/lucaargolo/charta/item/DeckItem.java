package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.DeckScreen;
import dev.lucaargolo.charta.client.item.DeckItemExt;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.lucaargolo.charta.resources.DeckResource.MISSING;

public class DeckItem extends Item {

    public DeckItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Deck deck = getDeck(stack);
        return deck != null ? deck.getName() : super.getName(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if(level.isClientSide()) {
            Deck deck = getDeck(stack);
            if(deck != null)
                openScreen(deck);
        }
        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(Deck deck) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DeckScreen(null, deck));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        Deck deck = getDeck(stack);
        if(deck != null) {
            tooltipComponents.add(Component.literal(String.valueOf(deck.getCards().size())).append(" ").append(Component.translatable("charta.cards")).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Nullable
    public static Deck getDeck(ItemStack stack) {

        CompoundTag tag = stack.getTag();


//        ResourceLocation deckId = stack.get(ModDataComponentTypes.CARD_DECK);
//        return deckId != null ? Charta.CARD_DECKS.getDeck(deckId) : null;
        if (tag == null)
            return MISSING;

        return Charta.CARD_DECKS.getDeck(ResourceLocation.parse(tag.getString("CARD_DECK")));
    }

    public static ItemStack getDeck(Deck deck) {
        ResourceLocation deckId = Charta.CARD_DECKS.getDecks()
            .entrySet()
            .stream()
            .filter(e -> e.getValue() == deck)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(Charta.id("missing"));
        return getDeck(deckId);
    }

    public static ItemStack getDeck(ResourceLocation deckId) {
        Deck deck = Charta.CARD_DECKS.getDeck(deckId);
        ItemStack stack = ModItems.DECK.get().getDefaultInstance();

        CompoundTag tag = new CompoundTag();
        tag.putString("CARD_DECK",deckId.toString());
        tag.putInt("RARITY",deck.getRarity().ordinal());
        stack.setTag(tag);
        ;
//        stack.set(ModDataComponentTypes.CARD_DECK, deckId);
//        stack.set(DataComponents.RARITY, deck.getRarity());
        return stack;
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer  renderer = new DeckItemExt(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}
