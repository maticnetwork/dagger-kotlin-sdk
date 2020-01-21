package network.matic.dagger

interface Callback {
    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    fun connectionLost(cause: Throwable?)
}