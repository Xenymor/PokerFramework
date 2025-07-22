package Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    final List<Card> cloneBoard = new ArrayList<Card>();
    int pot;

    List<Card> deck;
    int deckIndex = 0;
    int dealerIndex;

    final int playerCount;
    int activePlayerCount;
    int smallBlind;
    int bigBlind;

    boolean verbose;

    public Game(final List<String> configLines, final boolean verbose) {
        // Initialize the game with the provided configuration lines
        //TODO

        this.verbose = verbose;

        initializePlayers(configLines);
        initializeBlinds(configLines);

        playerCount = players.size();
        activePlayerCount = playerCount;
        active = new boolean[playerCount];
        folded = new boolean[playerCount];
        stacks = new int[playerCount];
        bets = new int[playerCount];
        cloneStacks = new int[playerCount];
        cloneBets = new int[playerCount];

        initializeStacks(configLines);

        board = new ArrayList<Card>();

        prepareCards();
    }

    private void initializeStacks(final List<String> configLines) {
        //TODO
        if (verbose) {
            System.out.println("Initializing stacks...");
        }
        for (int i = 0; i < playerCount; i++) {
            stacks[i] = 1000; // Example starting stack
            bets[i] = 0;
        }
    }

    private void initializeBlinds(final List<String> configLines) {
        //TODO
        if (verbose) {
            System.out.println("Initializing blinds...");
        }
        smallBlind = 5;
        bigBlind = 10;
    }

    private void initializeHands() {
        hands = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<Card>());
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
        List<Card> stack = new ArrayList<Card>(deck);
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
        deck = new ArrayList<Card>();
        for (int c = 0; c < 4; c++) {
            for (int i = 0; i < 13; i++) {
                deck.add(new Card(c, i));
            }
        }
    }

    private void initializePlayers(final List<String> configLines) {
        if (verbose) {
            System.out.println("Initializing players from configuration... " + configLines.size() + " lines.");
        }
        players = new ArrayList<Player>();
        players.add(new RandomBot());
        players.add(new RandomBot());
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

        int index = nextActivePlayerAfter(bigBlindIndex);
        int i = index;
        outer:
        for (int phase = 0; phase < 4; phase++) {
            do {
                if (verbose) {
                    System.out.println("Player " + i + " is taking their turn.");
                }
                final Player player = players.get(i);
                cloneStacks();
                cloneBets();
                cloneBoard();
                Action action = player.takeTurn(cloneBoard, cloneBets, cloneStacks, pot, i);
                takeAction(action, i);
                if (verbose) {
                    System.out.println("Player " + i + " took action: " + action);
                }
                i = nectactActivePlayerAfter(i);
                if (getActivePlayerCount() == 1) {
                    if (verbose) {
                        System.out.println("Only one player left active, ending round.");
                    }
                    break outer;
                }
            } while (i != index);
            if (verbose) {
                System.out.println("All players have taken their turn for phase " + phase + ".");
            }
            addBetsToPot();
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

        prepareCards();
    }

    private void prepareCards() {
        initializeDeck();
        initializeHands();
    }
}
