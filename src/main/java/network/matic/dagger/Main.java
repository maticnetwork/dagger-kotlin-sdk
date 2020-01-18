package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;

public class Main {
    public static void main(String[] args) throws DaggerException {
//        Dagger dagger = new Dagger("tcp://mainnet.dagger.matic.network:1884");
//        Dagger dagger = new Dagger("tcp://mqtt.eclipse.org:1883");

        Options options = new Options();
        options.setCallback(new Callback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost. Reason: " +  cause);
            }
        });

        Dagger dagger = new Dagger("tcp://localhost:1883", options);
        dagger.start();
        dagger.on("latest:block", new Listener() {
            @Override
            public void Callback(String topic, byte[] data) {
                System.out.println(topic);
            }
        });

        synchronized (dagger) {
            while(true) {
                try {
                    dagger.wait(5000);
                    System.out.println(String.format("Connected: %s, Subscriptions: %s", dagger.isConnected(), dagger.getSubscriptions()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
