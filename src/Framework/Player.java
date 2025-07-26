package Framework;

import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Player {
    final Process process;
    final Scanner processOutput;
    final BufferedReader errorOutput;
    final BufferedWriter processInput;
    private final boolean showOutputs;

    public Player(final String command, final boolean showOutputs) throws IOException {
        if (!command.isEmpty()) {
            process = Runtime.getRuntime().exec(command);
            processOutput = new Scanner(process.getInputStream());
            errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        } else {
            process = null;
            processOutput = null;
            errorOutput = null;
            processInput = null;
        }
        this.showOutputs = showOutputs;
    }

    public void newRound(final Card card1, final Card card2) {
        writeMessage("newround " + convertToString(card1) + " " + convertToString(card2) + "\n");
    }

    private String convertToString(final Card card) {
        return card.color() + " " + card.number();
    }

    public Action takeTurn(final List<Card> cloneBoard, final int[] cloneBets, final int[] cloneStacks, final int pot, final int currPlayerIndex) {
        final String builder = "turn " +
                convertToString(cloneBoard) +
                "," +
                convertToString(cloneBets) +
                "," +
                convertToString(cloneStacks) +
                "," +
                pot + "," +
                currPlayerIndex + "\n";
        writeMessage(builder);
        String answer = getAnswer();
        return parseAction(answer);
    }

    private Action parseAction(final String answer) {
        final String[] words = answer.split(" ");
        final String action = words[0];
        return switch (action.toUpperCase()) {
            case "CHECK" -> new Action(
                    ActionType.CHECK,
                    0
            );
            case "CALL" -> new Action(
                    ActionType.CALL,
                    0
            );
            case "RAISE" -> new Action(
                    ActionType.RAISE,
                    Integer.parseInt(words[1])
            );
            case "FOLD" -> new Action(
                    ActionType.FOLD,
                    0
            );
            default -> throw new IllegalArgumentException("Action " + action + " could not be parsed");
        };
    }

    private String getAnswer() {
        do {
            if (processOutput.hasNextLine()) {
                String answer = processOutput.nextLine();
                final String[] words = answer.split(" ");
                final String command = words[0];
                if (command.equalsIgnoreCase("Debug")) {
                    if (words.length > 1) {
                        for (int i = 1; i < words.length; i++) {
                            System.out.print(words[i] + " ");
                        }
                    }
                    System.out.println();
                } else {
                    if (showOutputs) {
                        System.out.println(answer);
                    }
                    return answer;
                }
            } else {
                errorOutput.lines().forEach(System.out::println);
                throw new NoSuchElementException("No line found in process output");
            }
        } while (true);
    }

    private void writeMessage(final String s) {
        try {
            final String str = s + "\n";
            if (showOutputs) {
                System.out.print(str);
            }
            processInput.write(str);
            processInput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertToString(final int[] cloneBets) {
        StringBuilder sb = new StringBuilder();
        for (int bet : cloneBets) {
            sb.append(bet).append(" ");
        }
        return sb.toString().trim();
    }

    private String convertToString(final List<Card> cloneBoard) {
        StringBuilder sb = new StringBuilder();
        for (Card card : cloneBoard) {
            sb.append(card).append(" ");
        }
        return sb.toString().trim();
    }
}
