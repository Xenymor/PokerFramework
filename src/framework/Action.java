package framework;

public record Action(ActionType type, int amount) {

    @Override
    public String toString() {
        //TODO
        return type.toString() + (amount > 0 ? " by " + amount : "");
    }
}
