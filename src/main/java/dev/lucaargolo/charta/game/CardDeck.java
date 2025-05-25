package dev.lucaargolo.charta.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.fun.FunGame;
import dev.lucaargolo.charta.utils.CardImageUtils;
import dev.lucaargolo.charta.utils.ExpandedStreamCodec;
import dev.lucaargolo.charta.utils.SuitImage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CardDeck {

    public static final CardDeck EMPTY = new CardDeck(Rarity.COMMON, false, List.of(), s -> Charta.MISSING_SUIT, s -> "suit.charta.unknown", c -> Charta.MISSING_CARD, c -> "card.charta.unknown", () -> Charta.MISSING_DECK, () -> "deck.charta.unknown");

    public static final StreamCodec<ByteBuf, CardDeck> STREAM_CODEC = ExpandedStreamCodec.composite(
        Rarity.STREAM_CODEC,
        CardDeck::getRarity,
        ByteBufCodecs.BOOL,
        CardDeck::isTradeable,
        ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC),
        CardDeck::getCards,
        ByteBufCodecs.map(HashMap::new, Suit.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getSuitsLocation,
        ByteBufCodecs.map(HashMap::new, Suit.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
        CardDeck::getSuitsTranslatableKeys,
        ByteBufCodecs.map(HashMap::new, Card.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getCardsLocation,
        ByteBufCodecs.map(HashMap::new, Card.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
        CardDeck::getCardsTranslatableKeys,
        ResourceLocation.STREAM_CODEC,
        CardDeck::getDeckLocation,
        ByteBufCodecs.STRING_UTF8,
        CardDeck::getDeckTranslatableKey,
        CardDeck::new
    );

    public static final Codec<CardDeck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Rarity.CODEC.fieldOf("rarity").forGetter(CardDeck::getRarity),
        Codec.BOOL.fieldOf("tradeable").forGetter(CardDeck::isTradeable),
        Card.CODEC.listOf().fieldOf("cards").forGetter(CardDeck::getCards),
        Codec.unboundedMap(Suit.CODEC, ResourceLocation.CODEC).fieldOf("suits_images").forGetter(CardDeck::getSuitsLocation),
        Codec.unboundedMap(Suit.CODEC, Codec.STRING).fieldOf("suits_keys").forGetter(CardDeck::getSuitsTranslatableKeys),
        Codec.unboundedMap(Card.CODEC, ResourceLocation.CODEC).fieldOf("cards_images").forGetter(CardDeck::getCardsLocation),
        Codec.unboundedMap(Card.CODEC, Codec.STRING).fieldOf("cards_keys").forGetter(CardDeck::getCardsTranslatableKeys),
        ResourceLocation.CODEC.fieldOf("deck_image").forGetter(CardDeck::getDeckLocation),
        Codec.STRING.fieldOf("deck_key").forGetter(CardDeck::getDeckTranslatableKey)
    ).apply(instance, CardDeck::new));

    private final Rarity rarity;
    private final boolean tradeable;
    private final ImmutableList<Card> cards;
    private final ImmutableSet<Card> uniqueCards;
    private final ImmutableSet<Suit> uniqueSuits;

    private final Function<Suit, ResourceLocation> suitsLocation;
    private final Function<Suit, String> suitsTranslatableKeys;

    private final Function<Card, ResourceLocation> cardsLocation;
    private final Function<Card, String> cardsTranslatableKeys;

    private final Supplier<ResourceLocation> deckLocation;
    private final Supplier<String> deckTranslatableKey;

    private CardDeck(Rarity rarity, boolean tradeable, List<Card> cards, Map<Suit, ResourceLocation> suitsLocation, Map<Suit, String> suitsTranslatableKey, Map<Card, ResourceLocation> cardsLocation, Map<Card, String> cardsTranslatableKey, ResourceLocation deckLocation, String deckTranslatableKey) {
        this(rarity, tradeable, cards, suit -> suitsLocation.getOrDefault(suit, Charta.MISSING_SUIT), suit -> suitsTranslatableKey.getOrDefault(suit, "suit.charta.unknown"), card -> cardsLocation.getOrDefault(card, Charta.MISSING_CARD), card -> cardsTranslatableKey.getOrDefault(card, "card.charta.unknown"), () -> deckLocation, () -> deckTranslatableKey);
    }

    public CardDeck(Rarity rarity, boolean tradeable, List<Card> cards, Function<Suit, ResourceLocation> suitsLocation, Function<Suit, String> suitsTranslatableKey, Function<Card, ResourceLocation> cardsLocation, Function<Card, String> cardsTranslatableKey, Supplier<ResourceLocation> deckLocation, Supplier<String> deckTranslatableKey) {
        this.rarity = rarity;
        this.tradeable = tradeable;
        this.cards = ImmutableList.copyOf(cards);
        this.uniqueCards = ImmutableSet.copyOf(cards);
        this.uniqueSuits = ImmutableSet.copyOf(cards.stream().map(Card::suit).iterator());
        this.suitsLocation = suitsLocation;
        this.suitsTranslatableKeys = suitsTranslatableKey;
        this.cardsLocation = cardsLocation;
        this.cardsTranslatableKeys = cardsTranslatableKey;
        this.deckLocation = deckLocation;
        this.deckTranslatableKey = deckTranslatableKey;
    }

    public Component getName() {
        return Component.translatable(deckTranslatableKey.get());
    }

    public ResourceLocation getSuitTexture(Suit suit, boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getSuitGlowTexture(suitsLocation.apply(suit));
        }else {
            return ChartaClient.getSuitTexture(suitsLocation.apply(suit));
        }
    }

    public String getSuitTranslatableKey(Suit suit) {
        return suitsTranslatableKeys.apply(suit);
    }

    public ResourceLocation getCardTexture(Card card, boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getCardGlowTexture(cardsLocation.apply(card));
        }else {
            return ChartaClient.getCardTexture(cardsLocation.apply(card));
        }
    }

    public String getCardTranslatableKey(Card card) {
        return cardsTranslatableKeys.apply(card);
    }

    public ResourceLocation getDeckTexture(boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getDeckGlowTexture(deckLocation.get());
        }else{
            return ChartaClient.getDeckTexture(deckLocation.get());
        }
    }

    //CODEC Getters

    public Rarity getRarity() {
        return rarity;
    }

    public boolean isTradeable() {
        return tradeable;
    }

    public ImmutableList<Card> getCards() {
        return cards;
    }

    public ImmutableSet<Card> getUniqueCards() {
        return uniqueCards;
    }

    public ImmutableSet<Suit> getUniqueSuits() {
        return uniqueSuits;
    }

    private Map<Suit, ResourceLocation> getSuitsLocation() {
        return Maps.asMap(uniqueSuits, suitsLocation::apply);
    }

    private Map<Suit, String> getSuitsTranslatableKeys() {
        return Maps.asMap(uniqueSuits, suitsTranslatableKeys::apply);
    }

    private Map<Card, ResourceLocation> getCardsLocation() {
        return Maps.asMap(uniqueCards, cardsLocation::apply);
    }

    private Map<Card, String> getCardsTranslatableKeys() {
        return Maps.asMap(uniqueCards, cardsTranslatableKeys::apply);
    }

    public ResourceLocation getDeckLocation() {
        return deckLocation.get();
    }

    public String getDeckTranslatableKey() {
        return deckTranslatableKey.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDeck deck = (CardDeck) o;
        return rarity == deck.rarity &&
            Objects.equals(cards, deck.cards) &&
            Objects.equals(getSuitsLocation(), deck.getSuitsLocation()) &&
            Objects.equals(getSuitsTranslatableKeys(), deck.getSuitsTranslatableKeys()) &&
            Objects.equals(getCardsLocation(), deck.getCardsLocation()) &&
            Objects.equals(getCardsTranslatableKeys(), deck.getCardsTranslatableKeys()) &&
            Objects.equals(deckLocation.get(), deck.deckLocation.get()) &&
            Objects.equals(deckTranslatableKey.get(), deck.deckTranslatableKey.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            rarity,
            cards,
            getSuitsLocation(),
            getSuitsTranslatableKeys(),
            getCardsLocation(),
            getCardsTranslatableKeys(),
            deckLocation.get(),
            deckTranslatableKey.get()
        );
    }

    public static CardDeck simple(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        return simple(rarity, canBeTraded, cardLocation, cardLocation, deckLocation);
    }

    public static CardDeck simple(Rarity rarity, boolean canBeTraded, ResourceLocation suitLocation, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Charta.DEFAULT_SUITS) {
            for (Rank rank : Charta.DEFAULT_RANKS) {
                deck.add(new Card(suit, rank));
            }
        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath().replace("/", ".");
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey =  "deck." + deckLocation.getNamespace() + "." + deckLocation.getPath().replace("/", ".");
        }
        String deckTranslatableKey = translatableKey;
        return new CardDeck(rarity, canBeTraded, deck, (suit) -> suitLocation.withSuffix("/" + suit.location().getPath()), (suit) -> "suit.charta."+suit.location().getPath(), (card) -> cardLocation.withSuffix( "/" + card.suit().location().getPath() + "_" + card.rank().location().getPath()), (card) -> "card.charta."+card.suit().location().getPath()+"."+card.rank().location().getPath(), () -> deckLocation, () -> deckTranslatableKey);
    }

    public static CardDeck fun(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        return fun(rarity, canBeTraded, cardLocation, cardLocation, deckLocation);
    }

    public static CardDeck fun(Rarity rarity, boolean canBeTraded, ResourceLocation suitLocation, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : FunGame.SUITS) {
            for (Rank rank : FunGame.RANKS) {
                deck.add(new Card(suit, rank));
                if(rank != Rank.WILD && rank != Rank.WILD_PLUS_4 && rank != Rank.ZERO) {
                    deck.add(new Card(suit, rank));
                }
            }

        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath().replace("/", ".");
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey =  "deck." + deckLocation.getNamespace() + "." + deckLocation.getPath().replace("/", ".");
        }
        String deckTranslatableKey = translatableKey;
        return new CardDeck(rarity, canBeTraded, deck, (suit) -> suitLocation.withSuffix("/" + suit.location().getPath()), (suit) -> "suit.charta."+suit.location().getPath(), (card) -> cardLocation.withSuffix( "/" + card.suit().location().getPath() + "_" + card.rank().location().getPath()), (card) -> card.rank() == Rank.WILD || card.rank() == Rank.WILD_PLUS_4 ? "card.charta."+card.rank().location().getPath() : "card.charta."+card.suit().location().getPath()+"."+card.rank().location().getPath(), () -> deckLocation, () -> deckTranslatableKey);
    }

    public int getCardColor(Card card) {
        return getSuitColor(card.suit());
    }

    public int getSuitColor(Suit suit) {
        SuitImage image = Charta.SUIT_IMAGES.getImages().getOrDefault(suitsLocation.apply(suit), CardImageUtils.EMPTY_SUIT);
        if(image == CardImageUtils.EMPTY_SUIT) {
            return 0xFFFFFF;
        }
        int color = image.getAverageColor();
        Vec3 col = Vec3.fromRGB24(color);
        double brightness = 0.299 * col.x + 0.587 * col.y + 0.114 * col.z;
        if (brightness < 0.5) {
            double factor = 2.5 * 255;
            int r = Math.min(255, (int)(col.x * factor));
            int g = Math.min(255, (int)(col.y * factor));
            int b = Math.min(255, (int)(col.z * factor));

            return (r << 16) | (g << 8) | b;
        }else{
            return color;
        }
    }
}
