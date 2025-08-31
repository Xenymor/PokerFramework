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
        Game game = new Game(players, 5, 10, 500, false);

        int[] scores = new int[players.size()];

        for (int i = 0; i < 100_000; i++) {
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
            game.resetGame();
        }

        System.out.println("Final scores after 100,000 games:");
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).getClass().getSimpleName() + " (Player " + i + "): " + scores[i]);
        }
    }
}
