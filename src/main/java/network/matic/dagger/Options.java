package network.matic.dagger;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class Options {
    private MqttConnectOptions mqttConnectOptions;

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }

    public void setMqttConnectOptions(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }
}
