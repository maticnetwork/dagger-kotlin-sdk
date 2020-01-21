package network.matic.dagger

interface Listener {
    fun callback(topic: String?, data: ByteArray?)
}
