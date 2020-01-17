package network.matic.dagger;

import network.matic.dagger.exceptions.DaggerException;

public class Main {
    public static void main(String[] args) throws DaggerException {
        MqttRegex m = new MqttRegex("hello/+/fdfdfd/fdfd/#");
        System.out.println(m.matches("hello/dfd/fdfdfd/fdfd/hello"));

        Dagger dagger = new Dagger("wss://mainnet.dagger.matic.network");
        dagger.start();
    }
}
