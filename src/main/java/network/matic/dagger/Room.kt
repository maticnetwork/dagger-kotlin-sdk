package network.matic.dagger

import network.matic.dagger.exceptions.DaggerException

class Room(dagger: Dagger?, roomType: RoomType?) {
    val roomType: RoomType
    val dagger: Dagger

    @Throws(DaggerException::class)
    fun on(eventName: String, listener: Listener?): Room {
        dagger.on("$roomType:$eventName", listener)
        return this
    }

    @Throws(DaggerException::class)
    fun off(eventName: String, listener: Listener?): Room {
        dagger.off("$roomType:$eventName", listener)
        return this
    }

    init {
        if (dagger == null) {
            throw DaggerException("`dagger` object is required")
        }
        if (roomType == null) {
            throw DaggerException("`room` is required")
        }
        this.dagger = dagger
        this.roomType = roomType
    }
}
