package Framework;

import Framework.Bots.Player;
import Framework.Bots.RandomBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    List<Player> players;
    boolean[] active;
    boolean[] folded;
    List<List<Card>> hands;
    List<Card> board;

    final int[] stacks;
    final int[] bets;
    final int[] cloneStacks;
    final int[] cloneBets;
    final List<Card> cloneBoard = new ArrayList<>();
    int pot;

    List<Card> deck;
    int deckIndex = 0;
    int dealerIndex;

    final int playerCount;
    int activePlayerCount;
    int smallBlind;
    int bigBlind;

    boolean verbose;

    public Game(final List<Player> players, int smallBlind, int bigBlind, int initialStackSize, final boolean verbose) {

        this.verbose = verbose;

        this.players = players;
        initializeBlinds(smallBlind, bigBlind);

        playerCount = this.players.size();
        activePlayerCount = playerCount;
        active = new boolean[playerCount];
        folded = new boolean[playerCount];
        Arrays.fill(active, true);
        Arrays.fill(folded, false);
        stacks = new int[playerCount];
        bets = new int[playerCount];
        cloneStacks = new int[playerCount];
        cloneBets = new int[playerCount];

        initializeStacks(initialStackSize);

        board = new ArrayList<>();

        prepareCards();
    }

    private void initializeStacks(final int initialStackSize) {
        if (verbose) {
            System.out.println("Initializing stacks...");
        }
        for (int i = 0; i < playerCount; i++) {
            stacks[i] = initialStackSize;
            bets[i] = 0;
        }
    }

    private void initializeBlinds(int smallBlind, int bigBlind) {
        if (verbose) {
            System.out.println("Initializing blinds...");
        }
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    private void initializeHands() {
        hands = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<>());
        }
    }

    private void initializeDeck() {
        if (verbose) {
            System.out.println("Initializing deck...");
        }
        createCards();
        shuffleDeck();
    }

    private void shuffleDeck() {
        List<Card> stack = new ArrayList<>(deck);
        deck.clear();
        while (!stack.isEmpty()) {
            int index = (int) (Math.random() * stack.size());
            deck.add(stack.remove(index));
        }
        if (verbose) {
            System.out.println("Deck shuffled: ");
            System.out.println("\t" + deck);
        }
    }

    private void createCards() {
        deck = new ArrayList<>();
        for (int c = 0; c < 4; c++) {
            for (int i = 0; i < 13; i++) {
                deck.add(new Card(c, i + 1));
            }
        }
    }

    public void startRound() {
        // Start a new round of the game
        if (verbose) {
            System.out.println("\nStarting a new round...");
            for (int i = 0; i < playerCount; i++) {
                System.out.println("Player " + i + " is " + players.get(i).getClass().getSimpleName() + ".");
            }
        }
        dealCards();
        for (int i = 0; i < playerCount; i++) {
            final List<Card> cards = hands.get(i);
            players.get(i).newRound(cards.get(0), cards.get(1));
        }
        if (verbose) {
            System.out.println("Dealt cards to players.");
        }

        int smallBlindIndex = nextActivePlayerAfter(dealerIndex);
        raise(smallBlindIndex, smallBlind);
        int bigBlindIndex = nextActivePlayerAfter(smallBlindIndex);
        raise(bigBlindIndex, bigBlind);

        int startingPlayerIndex = nextActivePlayerAfter(bigBlindIndex);
        int currPlayerIndex = startingPlayerIndex;
        int toCall = bigBlind;
        outer:
        for (int phase = 0; phase < 4; phase++) {
            do {
                if (verbose) {
                    System.out.println("Player " + currPlayerIndex + " is taking their turn.");
                }
                final Player player = players.get(currPlayerIndex);
                cloneStacks();
                cloneBets();
                cloneBoard();
                if (verbose) {
                    System.out.println("Current pot: " + pot);
                }

                Action action = player.takeTurn(cloneBoard, cloneBets, cloneStacks, pot, currPlayerIndex);
                startingPlayerIndex = takeAction(action, currPlayerIndex, startingPlayerIndex, toCall);

                if (bets[currPlayerIndex] > toCall) {
                    toCall = bets[currPlayerIndex];
                }

                if (verbose) {
                    System.out.println("Player " + currPlayerIndex + " took action: " + action);
                }

                currPlayerIndex = nextActivePlayerAfter(currPlayerIndex);
                if (getActivePlayerCount() == 1) {
                    if (verbose) {
                        System.out.println("Only one player left active, ending round.");
                    }
                    break outer;
                }
            } while (currPlayerIndex != startingPlayerIndex);
            if (verbose) {
                System.out.println("All players have taken their turn for phase " + phase + ".");
            }
            addBetsToPot();
            toCall = 0;
            switch (phase) {
                case 0: // Pre-flop
                    if (verbose) {
                        System.out.println("Dealing the flop...");
                    }
                    board.addAll(deck.subList(deckIndex, deckIndex + 3));
                    deckIndex += 3;
                    break;
                case 1: // Flop
                    if (verbose) {
                        System.out.println("Dealing the turn...");
                    }
                    board.add(deck.get(deckIndex++));
                    break;
                case 2: // Turn
                    if (verbose) {
                        System.out.println("Dealing the river...");
                    }
                    board.add(deck.get(deckIndex++));
                    break;
                case 3: // River
                    if (verbose) {
                        System.out.println("Finalizing round...");
                    }
                    break;
            }
        }

        if (verbose) {
            System.out.println("Round ended. Board: " + board);
        }
        int[] winners = getWinners();
        splitPot(winners);
        eliminatePlayers();
    }

    private void addBetsToPot() {
        for (int i = 0; i < playerCount; i++) {
            pot += bets[i];
            bets[i] = 0; // Reset bets after adding to pot
        }
        if (verbose) {
            System.out.println("Total pot after adding bets: " + pot);
        }
    }

    private void eliminatePlayers() {
        for (int i = 0; i < playerCount; i++) {
            if (stacks[i] <= 0) {
                folded[i] = true;
                active[i] = false;
                activePlayerCount--;
                if (verbose) {
                    System.out.println("Player " + i + " has been eliminated.");
                }
            }
        }
    }

    private void splitPot(final int[] winners) {
        for (int winnerIndex : winners) {
            int winnings = pot / winners.length;
            stacks[winnerIndex] += winnings;
            if (verbose) {
                System.out.println("Player " + winnerIndex + " wins " + winnings + " chips.");
            }
        }
        pot = 0; // Reset pot after splitting
    }

    private int[] getWinners() {
        List<Integer> activePlayers = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            if (!folded[i] && active[i]) {
                activePlayers.add(i);
            }
        }
        if (activePlayers.isEmpty()) {
            return new int[0]; // No winners
        }
        long bestScore = -1;
        List<Integer> winners = new ArrayList<>();
        for (int playerIndex : activePlayers) {
            long score = evaluateHand(playerIndex);
            if (score > bestScore) {
                bestScore = score;
                winners.clear();
                winners.add(playerIndex);
            } else if (score == bestScore) {
                winners.add(playerIndex);
            }
        }
        return winners.stream().mapToInt(i -> i).toArray();
    }

    public long evaluateHand(final int playerIndex) {
        List<Card> playerHand = hands.get(playerIndex);
        List<Card> combined = new ArrayList<>(board);
        combined.addAll(playerHand);

        Collections.sort(combined);
        int[] counts = new int[14];
        int[] colorCounts = new int[4];

        if (verbose) {
            System.out.println("Evaluating hand for player " + playerIndex + ": " + combined);
        }

        countCards(combined, counts, colorCounts);

        int[] countCounts = new int[4];
        for (int i = 1; i < counts.length; i++) {
            final int count = counts[i];
            if (count > 0) {
                countCounts[count - 1]++;
            }
        }

        List<Integer> straights = getStraights(counts);
        boolean isFlush = isFlush(colorCounts);

        int multiplier;
        if (isStraightFlush(combined, colorCounts)) {
            multiplier = 9; // Straight Flush
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has a Straight Flush.");
            }
        } else if (countCounts[3] > 0) {
            multiplier = 8; // Four of a Kind
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has Four of a Kind.");
            }
        } else if ((countCounts[2] > 0 && countCounts[1] > 0) || countCounts[2] > 1) {
            multiplier = 7; // Full House
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has a Full House.");
            }
        } else if (isFlush) {
            multiplier = 6; // Flush
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has a Flush.");
            }
        } else if (straights.size() > 0) {
            multiplier = 5; // Straight
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has a Straight.");
            }
        } else if (countCounts[2] > 0) {
            multiplier = 4; // Three of a Kind
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has Three of a Kind.");
            }
        } else if (countCounts[1] > 1) {
            multiplier = 3; // Two Pair
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has Two Pair.");
            }
        } else if (countCounts[1] > 0) {
            multiplier = 2; // One Pair
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has One Pair.");
            }
        } else {
            multiplier = 1; // High Card
            if (verbose) {
                System.out.println(" \tPlayer " + playerIndex + " has a High Card.");
            }
        }

        List<Card> bestCards = getBestCards(combined, counts, colorCounts, multiplier, straights);
        Collections.reverse(bestCards);  // ??
        long score = bestCards.get(bestCards.size() - 1).number() * ((long) Math.pow(14, multiplier));
        if ((multiplier == 5 || multiplier == 9) && bestCards.get(bestCards.size() - 1).number() == 13) {
            score = bestCards.get(bestCards.size() - 2).number() * ((long) Math.pow(14, multiplier)); // Ace low
        }
        for (int i = bestCards.size() - 2; i >= 0; i--) {
            score *= 14;
            score += bestCards.get(i).number();
        }
        if (verbose) {
            System.out.println(" \tPlayer " + playerIndex + " has score: " + score);
        }
        return score;
    }

    public static long evaluateHand(List<Card> combined) {
        Collections.sort(combined);
        int[] counts = new int[14];
        int[] colorCounts = new int[4];

        countCards(combined, counts, colorCounts);

        int[] countCounts = new int[4];
        for (int i = 1; i < counts.length; i++) {
            final int count = counts[i];
            if (count > 0) {
                countCounts[count - 1]++;
            }
        }

        List<Integer> straights = getStraights(counts);
        boolean isFlush = isFlush(colorCounts);

        int multiplier;
        if (isStraightFlush(combined, colorCounts)) {
            multiplier = 9; // Straight Flush
        } else if (countCounts[3] > 0) {
            multiplier = 8; // Four of a Kind
        } else if ((countCounts[2] > 0 && countCounts[1] > 0) || countCounts[2] > 1) {
            multiplier = 7; // Full House
        } else if (isFlush) {
            multiplier = 6; // Flush
        } else if (straights.size() > 0) {
            multiplier = 5; // Straight
        } else if (countCounts[2] > 0) {
            multiplier = 4; // Three of a Kind
        } else if (countCounts[1] > 1) {
            multiplier = 3; // Two Pair
        } else if (countCounts[1] > 0) {
            multiplier = 2; // One Pair
        } else {
            multiplier = 1; // High Card
        }

        List<Card> bestCards = getBestCards(combined, counts, colorCounts, multiplier, straights);
        Collections.reverse(bestCards);  // ??
        long score = bestCards.get(bestCards.size() - 1).number() * ((long) Math.pow(14, multiplier));
        if ((multiplier == 5 || multiplier == 9) && bestCards.get(bestCards.size() - 1).number() == 13) {
            score = bestCards.get(bestCards.size() - 2).number() * ((long) Math.pow(14, multiplier)); // Ace low
        }
        for (int i = bestCards.size() - 2; i >= 0; i--) {
            score *= 14;
            score += bestCards.get(i).number();
        }
        return score;
    }

    private static void countCards(final List<Card> combined, final int[] counts, final int[] colorCounts) {
        for (final Card card : combined) {
            counts[card.number()]++;
            colorCounts[card.color()]++;
            if (card.number() == 13) {
                counts[0]++; // Ace can be high or low
            }
        }
    }

    private static boolean isStraightFlush(List<Card> combined, final int[] colorCounts) {
        int mostCommonColor = getMostCommonColor(colorCounts);
        combined = combined.stream()
                .filter(card -> card.color() == mostCommonColor)
                .collect(Collectors.toList());
        for (int i = -1; i < combined.size() - 5; i++) {
            List<Card> curr = new ArrayList<>(combined.subList(Math.max(i, 0), i + 5));
            if (i == -1) {
                final Card highestCard = combined.get(combined.size() - 1);
                if (highestCard.number() == 13) {
                    curr.add(0, highestCard);
                } else {
                    continue;
                }
            }
            int[] counts = new int[14];
            int[] currColorCounts = new int[4];
            countCards(curr, counts, currColorCounts);
            List<Integer> currStraights = getStraights(counts);
            if (!currStraights.isEmpty()) {
                return true; // Found a straight flush
            }
        }
        return false;
    }

    private static void filterForColor(final List<Card> combined, final int mostCommonColor) {
        for (int i = combined.size() - 1; i >= 0; i--) {
            if (combined.get(i).color() != mostCommonColor) {
                combined.remove(i);
            }
        }
    }

    private static int getMostCommonColor(final int[] colorCounts) {
        int mostCommonColor = -1;
        int maxColorCount = 0;
        for (int i = 0; i < colorCounts.length; i++) {
            if (colorCounts[i] > maxColorCount) {
                maxColorCount = colorCounts[i];
                mostCommonColor = i;
            }
        }
        return mostCommonColor;
    }

    /**
     * Returns combination of 5 best cards; last Card is lowest
     */
    private static List<Card> getBestCards(final List<Card> combined, final int[] counts, final int[] colorCounts, final int multiplier, final List<Integer> straights) {
        List<Card> bestCards = new ArrayList<>();
        switch (multiplier) {
            case 1: // High Card
                fillBestCards(combined, bestCards);
                break;
            case 2: // One Pair
                addHighestPair(combined, counts, bestCards);
                fillBestCards(combined, bestCards);
                break;
            case 3: // Two Pair
                addHighestPair(combined, counts, bestCards);
                addHighestPair(combined, counts, bestCards);
                fillBestCards(combined, bestCards);
                break;
            case 4: // Three of a Kind
                addHighestTriplet(combined, counts, bestCards);
                fillBestCards(combined, bestCards);
                break;
            case 5: // Straight
                addHighestStraight(combined, straights, bestCards);
                break;
            case 6: // Flush
                int mostCommonColor = getMostCommonColor(colorCounts);
                for (int i = combined.size() - 1; i >= 0 && combined.size() < 5; i--) {
                    if (combined.get(i).color() == mostCommonColor) {
                        bestCards.add(combined.remove(i));
                    }
                }
                break;
            case 7: // Full House
                addHighestTriplet(combined, counts, bestCards);
                addHighestPair(combined, counts, bestCards);
                break;
            case 8: // Four of a Kind
                addHighestQuadruplet(combined, counts, bestCards);
                fillBestCards(combined, bestCards);
                break;
            case 9: // Straight Flush
                mostCommonColor = getMostCommonColor(colorCounts);
                filterForColor(combined, mostCommonColor);
                int[] currCounts = new int[14];
                countCards(combined, currCounts, colorCounts);
                List<Integer> currStraights = getStraights(counts);
                addHighestStraight(combined, currStraights, bestCards);
                break;
            default:
                throw new IllegalStateException("Unexpected multiplier: " + multiplier);
        }
        return bestCards;
    }

    private static void addHighestStraight(final List<Card> combined, final List<Integer> straights, final List<Card> bestCards) {
        int straightStart = straights.get(straights.size() - 1);
        if (straightStart == 0) {
            bestCards.add(combined.remove(combined.size() - 1)); // Ace low straight
            straightStart = 1; // Adjust to start from 1
        }
        int index = 0;
        for (int i = straightStart; i < straightStart + 5 && bestCards.size() < 5; i++) {
            for (int j = index; j < combined.size(); j++, index++) {
                if (combined.get(j).number() == i) {
                    bestCards.add(combined.remove(j));
                    break;
                }
            }
        }
    }

    private static void addHighestQuadruplet(final List<Card> combined, final int[] counts, final List<Card> bestCards) {
        for (int i = counts.length - 1; i >= 0; i--) {
            if (counts[i] >= 4) {
                for (int j = combined.size() - 1; j >= 0; j--) {
                    if (combined.get(j).number() == i) {
                        bestCards.add(combined.remove(j - 3));
                        bestCards.add(combined.remove(j - 3));
                        bestCards.add(combined.remove(j - 3));
                        bestCards.add(combined.remove(j - 3));
                        break;
                    }
                }
                break;
            }
        }
    }

    private static void addHighestTriplet(final List<Card> combined, final int[] counts, final List<Card> bestCards) {
        for (int i = counts.length - 1; i >= 0; i--) {
            if (counts[i] >= 3) {
                for (int j = combined.size() - 1; j >= 0; j--) {
                    if (combined.get(j).number() == i) {
                        bestCards.add(combined.remove(j - 2));
                        bestCards.add(combined.remove(j - 2));
                        bestCards.add(combined.remove(j - 2));
                        break;
                    }
                }
                counts[i] -= 3; // Remove triplet from counts
                break;
            }
        }
    }

    private static void fillBestCards(final List<Card> combined, final List<Card> bestCards) {
        while (bestCards.size() < 5 && !combined.isEmpty()) {
            bestCards.add(combined.remove(combined.size() - 1));
        }
    }

    private static void addHighestPair(final List<Card> combined, final int[] counts, final List<Card> bestCards) {
        for (int i = counts.length - 1; i >= 0; i--) {
            if (counts[i] >= 2) {
                for (int j = combined.size() - 1; j >= 0; j--) {
                    if (combined.get(j).number() == i) {
                        bestCards.add(combined.remove(j - 1));
                        bestCards.add(combined.remove(j - 1));
                        break;
                    }
                }
                counts[i] -= 2; // Remove pair from counts
                break;
            }
        }
    }

    private static List<Integer> getStraights(final int[] counts) {
        List<Integer> straightStarts = new ArrayList<>();
        int consecutive = 0;
        for (int i = 0; i < counts.length; i++) {
            final int count = counts[i];
            if (count > 0) {
                consecutive++;
                if (consecutive >= 5) {
                    straightStarts.add(i - 5 + 1);
                }
            } else {
                consecutive = 0;
            }
        }
        return straightStarts;
    }

    private static boolean isFlush(final int[] colorCounts) {
        for (int count : colorCounts) {
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    private int getActivePlayerCount() {
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (!folded[i] && active[i]) {
                count++;
            }
        }
        return count;
    }

    private void raise(final int playerIndex, int amount) {
        if (amount > stacks[playerIndex]) {
            amount = stacks[playerIndex];
        }
        if (amount < 0) {
            amount = 0;
        }
        bets[playerIndex] += amount;
        stacks[playerIndex] -= amount;
    }

    private int takeAction(final Action action, final int playerIndex, final int startingPlayerIndex, final int toCall) {
        if (verbose) {
            System.out.println("Player " + playerIndex + " is taking action: " + action);
        }
        int result = startingPlayerIndex;
        int localToCall = toCall - bets[playerIndex];
        switch (action.type()) {
            case FOLD -> {
                folded[playerIndex] = true;
                if (verbose) {
                    System.out.println("Player " + playerIndex + " folded.");
                }
            }
            case CHECK -> {
                if (localToCall > 0) {
                    throw new IllegalArgumentException("Player " + playerIndex + " cannot check, must call or raise.");
                }
                if (verbose) {
                    System.out.println("Player " + playerIndex + " checked.");
                }
            }
            case CALL -> {
                if (localToCall > stacks[playerIndex]) {
                    localToCall = stacks[playerIndex];
                }
                bets[playerIndex] += localToCall;
                stacks[playerIndex] -= localToCall;
                if (verbose) {
                    System.out.println("Player " + playerIndex + " called with bet of " + localToCall);
                }
            }
            case RAISE -> {
                int raiseAmount = action.amount();

                // Ensure raise amount is at least the bet to call
                if (raiseAmount <= localToCall) {
                    raiseAmount = localToCall;
                } else {
                    result = playerIndex;
                }

                // Ensure raise amount does not exceed player's stack
                if (raiseAmount > stacks[playerIndex]) {
                    raiseAmount = stacks[playerIndex];
                }
                bets[playerIndex] += raiseAmount;
                stacks[playerIndex] -= raiseAmount;
                if (verbose) {
                    System.out.println("Player " + playerIndex + " raised by " + raiseAmount);
                }
            }
            default -> throw new IllegalArgumentException("Unknown action type: " + action.type());
        }
        return result;
    }

    private int nextActivePlayerAfter(final int startIndex) {
        for (int i = 0; i < playerCount; i++) {
            int index = (startIndex + i + 1) % playerCount;
            if (active[index] && !folded[index]) {
                return index;
            }
        }
        // No active player found
        throw new IllegalStateException("No active player found after index " + startIndex);
    }

    private void cloneBoard() {
        cloneBoard.clear();
        cloneBoard.addAll(board);
        if (verbose) {
            System.out.println("Cloned board: " + cloneBoard);
        }
    }

    private void cloneBets() {
        if (playerCount >= 0) {
            System.arraycopy(bets, 0, cloneBets, 0, playerCount);
        }
        if (verbose) {
            System.out.println("Cloned bets: " + Arrays.toString(cloneBets));
        }
    }

    private void cloneStacks() {
        if (playerCount >= 0) {
            System.arraycopy(stacks, 0, cloneStacks, 0, playerCount);
        }
        if (verbose) {
            System.out.println("Cloned stacks: " + Arrays.toString(cloneStacks));
        }
    }

    private void dealCards() {
        for (int i = 0; i < playerCount; i++) {
            final List<Card> hand = hands.get(i);
            for (int j = 0; j < 2; j++) {
                hand.add(deck.remove(0));
            }
        }
        if (verbose) {
            for (int i = 0; i < playerCount; i++) {
                System.out.println("Player " + i + " hand: " + hands.get(i));
            }
        }
    }

    public void prepareRound() {
        deckIndex = 0;
        dealerIndex = (dealerIndex + 1) % playerCount;
        board.clear();
        hands.clear();
        for (int i = 0; i < playerCount; i++) {
            bets[i] = 0;
        }
        Arrays.fill(folded, false);
        prepareCards();
    }

    private void prepareCards() {
        initializeDeck();
        initializeHands();
    }

    public void printResult() {
        System.out.println("Final stacks after round:");
        for (int i = 0; i < playerCount; i++) {
            System.out.println(" \t" + i + ": " + stacks[i]);
        }
    }
}
