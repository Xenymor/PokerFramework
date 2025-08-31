package bots;

import framework.Action;
import framework.Card;

import java.util.List;

public class AllInBot  extends Player {
    @Override
    public Action takeTurn(final List<Card> cloneBoard, final int[] cloneBets, final int[] cloneStacks, final int pot, final int currPlayerIndex) {
        //Will be treated as check after first time
        return new Action(framework.ActionType.RAISE, cloneStacks[currPlayerIndex]);
    }
}
