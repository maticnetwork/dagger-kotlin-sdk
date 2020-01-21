package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;

public class Main {
    public static void main(String[] args) throws DaggerException {

        Options options = new Options();
        options.setCallback(new Callback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost. Reason: " + cause);
            }
        });

        Dagger dagger = new Dagger("tcp://ropsten.dagger.matic.network");
        dagger.start();
        dagger.on("latest:block", new Listener() {
            @Override
            public void callback(String topic, byte[] data) {
                System.out.println(new String(data));
            }
        });

        synchronized (dagger) {
            while (true) {
                try {
                    dagger.wait(5000);
                    System.out.println(String.format("Connected: %s, Subscriptions: %s", dagger.isConnected(),
                            dagger.getAllSubscriptions()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
