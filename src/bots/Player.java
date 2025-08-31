package bots;

import framework.Action;
import framework.Card;

import java.util.List;

public abstract class Player {
    final Card[] hand;

    public Player() {
        this.hand = new Card[2];
    }

    public void newRound(final Card card1, final Card card2) {
        this.hand[0] = card1;
        this.hand[1] = card2;
    }

    public abstract Action takeTurn(final List<Card> cloneBoard, final int[] cloneBets, final int[] cloneStacks, final int pot, final int currPlayerIndex, final int toCall);

}
