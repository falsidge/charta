package dev.lucaargolo.charta.resources;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Rarity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CardDeckResource implements ResourceManagerReloadListener {

    private static final Deck MISSING = Deck.simple(Rarity.COMMON, false, Charta.MISSING_CARD, Charta.MISSING_CARD);

    private LinkedHashMap<ResourceLocation, Deck> decks = new LinkedHashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        decks.clear();

        manager.listResources("decks", id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            try(InputStream stream = resource.open()) {
                ResourceLocation location = id.withPath(s -> s.replace("decks/", "").replace(".json", ""));
                try(InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    DataResult<Deck> cardDeck = Deck.CODEC.parse(JsonOps.INSTANCE, json);
                    decks.put(location, cardDeck.getOrThrow());
                }
            }catch (IOException e) {
                Charta.LOGGER.error("Error while reading deck {} :", id, e);
            }
        });

        //Sort it so it looks great in the creative menu.
        decks = decks.entrySet().stream().sorted(Comparator.comparing((Map.Entry<ResourceLocation, Deck> entry) -> entry.getValue().isTradeable()).reversed()
            .thenComparing(entry -> entry.getValue().getRarity().ordinal())
            .thenComparing(entry -> entry.getValue().getCards().size())
            .thenComparing(Map.Entry::getKey)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Charta.LOGGER.info("Loaded {} decks", decks.size());
    }

    public HashMap<ResourceLocation, Deck> getDecks() {
        return decks;
    }

    public void setDecks(LinkedHashMap<ResourceLocation, Deck> decks) {
        this.decks = decks;
    }

    public Deck getDeck(ResourceLocation id) {
        return decks.getOrDefault(id, MISSING);
    }

}
