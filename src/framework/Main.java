package framework;

import bots.AllInBot;
import bots.Player;
import bots.RandomBot;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Initialize the game
        ArrayList<Player> players = new ArrayList<>();
        players.add(new RandomBot());
        players.add(new RandomBot());
        players.add(new AllInBot());
        Game game = new Game(players, 5, 10, 500, true);

        int[] scores = new int[players.size()];

        while (game.activePlayerCount > 1) {
            game.prepareRound();

            // Start the game loop
            game.startRound();

            // Print the result of the game
            game.printResult();
            int winner = game.getWinner();
            if (winner != -1) {
                System.out.println(players.get(winner).getClass().getSimpleName() + " (Player " + winner + ") won");
                scores[winner]++;
            }
        }
    }
}
