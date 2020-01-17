package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;

public class Room {
    private RoomType roomType;
    private Dagger dagger;

    public Room(Dagger dagger, RoomType roomType) throws DaggerException {
        if (dagger == null) {
            throw new DaggerException("`dagger` object is required");
        }

        if (roomType == null) {
            throw new DaggerException("`room` is required");
        }

        this.dagger = dagger;
        this.roomType = roomType;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public Dagger getDagger() {
        return dagger;
    }

    public Room on(String eventName, Listener listener) throws DaggerException {
        this.dagger.on(this.roomType.toString() + ":" + eventName, listener);
        return this;
    }

    public Room off(String eventName, Listener listener) throws DaggerException {
        this.dagger.off(this.roomType.toString() + ":" + eventName, listener);
        return this;
    }
}
