package Framework;

import Framework.Bots.Player;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Initialize the game
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Framework.Bots.RandomBot());
        players.add(new Framework.Bots.RandomBot());
        Game game = new Game(players, 5, 10, 450, true);

        game.prepareRound();

        // Start the game loop
        game.startRound();

        // Print the result of the game
        game.printResult();
    }
}
