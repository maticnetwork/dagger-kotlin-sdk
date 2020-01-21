package network.matic.dagger

enum class RoomType(val room: String) {
    LATEST("latest"), CONFIRMED("confirmed");

    override fun toString(): String {
        return room
    }
}
