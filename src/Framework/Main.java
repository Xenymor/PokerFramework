package Framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        // Initialize the game
        //TODO read the file
        Game game = new Game(new ArrayList<>(), true);

        game.prepareRound();

        // Start the game loop
        game.startRound();

        // Print the result of the game
        game.printResult();
    }
}
