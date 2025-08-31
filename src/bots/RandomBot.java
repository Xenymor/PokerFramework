package bots;

import framework.Action;
import framework.ActionType;
import framework.Card;

import java.util.List;
import java.util.Random;

public class RandomBot extends Player {
    final Random randomGenerator;

    public RandomBot() {
        super();
        randomGenerator = new Random();
    }

    public Action takeTurn(final List<Card> board, final int[] bets, final int[] stacks, final int pot, final int playerIndex, final int toCall) {
        /*If you want to score your hand:
        List<Card> combinedCards = new ArrayList<>(board);
        combinedCards.add(hand[0]);
        combinedCards.add(hand[1]);
        long score = Game.evaluateHand(combinedCards);*/

        // Randomly choose an action
        double random = randomGenerator.nextDouble();
        if (toCall <= bets[playerIndex]) {
            // If the player can check or call
            if (random < 0.5) {
                return new Action(ActionType.CHECK, 0);
            } else {
                return new Action(ActionType.RAISE, (int) (Math.random() * stacks[playerIndex]));
            }
        } else {
            // If the player needs to call, raise or fold
            if (random < 0.5) {
                return new Action(ActionType.CALL, toCall - bets[playerIndex]);
            } else if (random < .75) {
                return new Action(ActionType.RAISE, (int) (Math.random() * stacks[playerIndex]));
            } else {
                return new Action(ActionType.FOLD, 0);
            }
        }
    }

}
