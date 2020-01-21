package network.matic.dagger

import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

open class Options {
    var clientId: String? = null
    var callback: Callback? = null
    var mqttConnectOptions: MqttConnectOptions? = null
    var mqttClientPersistence: MqttClientPersistence? = null
}