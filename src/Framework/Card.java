package Framework;

public record Card(int color, int number) implements Comparable<Card> {

    @Override
    public int compareTo(final Card o) {
        if (this.number != o.number) {
            return Integer.compare(this.number, o.number);
        }
        return Integer.compare(this.color, o.color);
    }

    @Override
    public String toString() {
        return colorToString(color) + number + colorToString(-1);
    }

    private String colorToString(final int color) {
        switch (color) {
            case 0 -> {
                return "\u001B[31m"; // Red
            }
            case 1 -> {
                return "\u001B[32m"; // Green
            }
            case 2 -> {
                return "\u001B[34m"; // Blue
            }
            case 3 -> {
                return "\u001B[33m"; // Yellow
            }
            default -> {
                return "\u001B[0m"; // Reset color
            }
        }
    }
}
