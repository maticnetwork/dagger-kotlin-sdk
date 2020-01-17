package network.matic.dagger;

public interface Listener {
    void Callback(String topic, byte[] data);
}
