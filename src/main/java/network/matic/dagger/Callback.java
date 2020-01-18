package network.matic.dagger;

public interface Callback {
    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    public void connectionLost(Throwable cause);
}
