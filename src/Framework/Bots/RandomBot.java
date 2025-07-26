package Framework.Bots;

import Framework.Action;
import Framework.ActionType;
import Framework.Card;

import java.util.List;

public class RandomBot extends Player {
    public RandomBot() {
        super();
    }

    public Action takeTurn(final List<Card> board, final int[] bets, final int[] stacks, final int pot, final int playerIndex) {
        int maxBet = getMaxBet(bets);
        // Randomly choose an action
        double random = Math.random();
        if (maxBet <= bets[playerIndex]) {
            // If the player can check or call
            if (random < 0.5) {
                return new Action(ActionType.CHECK, 0);
            } else {
                return new Action(ActionType.RAISE, (int) (Math.random() * stacks[playerIndex]));
            }
        } else {
            // If the player needs to call or fold
            if (random < 0.5) {
                return new Action(ActionType.CALL, maxBet - bets[playerIndex]);
            } else {
                return new Action(ActionType.RAISE, (int) (Math.random() * stacks[playerIndex]));
            }
        }
    }

    private int getMaxBet(final int[] bets) {
        int maxBet = 0;
        for (int bet : bets) {
            if (bet > maxBet) {
                maxBet = bet;
            }
        }
        return maxBet;
    }
}
