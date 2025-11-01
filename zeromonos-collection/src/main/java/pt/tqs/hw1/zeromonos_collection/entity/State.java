package pt.tqs.hw1.zeromonos_collection.entity;

public enum State {
    RECEIVED,
    ASSIGNED,
    IN_PROGRESS,
    DONE,
    CANCELED;

    public boolean canTransitionTo(State next) {
        return switch (this) {
            case RECEIVED -> next == ASSIGNED || next == CANCELED;
            case ASSIGNED -> next == IN_PROGRESS || next == CANCELED;
            case IN_PROGRESS -> next == DONE || next == CANCELED;
            case DONE, CANCELED -> false;
        };
    }
}
