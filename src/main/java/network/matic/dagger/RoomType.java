package network.matic.dagger;

public enum RoomType {
    LATEST("latest"),
    CONFIRMED("confirmed");

    private final String room;

    private RoomType(String room) {
        this.room = room;
    }

    public String toString() {
        return this.room;
    }
    
    public String getRoom() {
        return this.room;
    }
}
