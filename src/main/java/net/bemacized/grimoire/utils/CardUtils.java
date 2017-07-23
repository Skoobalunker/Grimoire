package net.bemacized.grimoire.utils;

import com.sun.istack.internal.NotNull;
import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.resource.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CardUtils {

    private final static int MAX_ALTERNATIVES = 20;

    public static Card getCard(@NotNull String name) throws TooManyResultsException, MultipleResultsException, NoResultsException {
        return getCard(name, null);
    }

    public static Card getCard(@NotNull String name, String setCode) throws TooManyResultsException, MultipleResultsException, NoResultsException {
        // Create search for the card
        CardSearchQuery query = new CardSearchQuery().setName(name);
        // Specify set if provided
        if (setCode != null) query = query.setSetCode(setCode);
        // Execute search
        List<Card> cards = query.exec();
        // Quit if there are no results
        if (cards.isEmpty()) throw new NoResultsException();
        // Find single match
        if (cards.size() == 1) return cards.get(0);
        else {
            // Find exact match (of the most recent set
            Card card = cards.stream().filter(c -> c.getName().equalsIgnoreCase(name)).reduce((a, b) -> b).orElse(null);
            if (card != null) return card;
            // If none found return alternatives
            // Get the newest distinct results
            Collections.reverse(cards);
            cards = cards.stream().filter(ExtraStreamUtils.distinctByKey(Card::getName)).collect(Collectors.toList());
            // Quit if too many results
            if (cards.size() > MAX_ALTERNATIVES) throw new TooManyResultsException(cards);
            else throw new MultipleResultsException(cards);
        }
    }

    public static class NoResultsException extends Exception {
    }

    public static class MultipleResultsException extends Exception {
        private List<Card> results;

        MultipleResultsException(List<Card> alternatives) {
            this.results = alternatives;
        }

        public List<Card> getResults() {
            return results;
        }
    }

    public static class TooManyResultsException extends Exception {

        private List<Card> results;

        TooManyResultsException(List<Card> alternatives) {
            this.results = alternatives;
        }

        public List<Card> getResults() {
            return results;
        }
    }


    public static class CardSearchQuery {

        private List<String> filters;

        public CardSearchQuery() {
            filters = new ArrayList<>();
        }

        public CardSearchQuery setName(String name) {
            filters.add("name=" + name);
            return this;
        }

        public CardSearchQuery setSetCode(String setCode) {
            filters.add("set=" + setCode);
            return this;
        }

        public CardSearchQuery setExactName(String name) {
            filters.add("name=\"" + name + "\"");
            return this;
        }

        public List<Card> exec() {
            return CardAPI.getAllCards(filters);
        }
    }

}