package framework;

public enum ActionType {
    CHECK, CALL, RAISE, FOLD;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
