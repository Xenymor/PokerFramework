package Framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        // Initialize the game
        Game game = new Game(Files.readAllLines(Path.of(args[0])), true);

        game.prepareRound();

        // Start the game loop
        game.startRound();

        // Print the result of the game
        game.printResult();
    }
}
