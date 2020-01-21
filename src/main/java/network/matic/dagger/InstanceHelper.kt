package network.matic.dagger

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

interface InstanceHelper {
    fun getMqttClient(serverUri: String, clientId: String, mqttClientPersistence: MqttClientPersistence): MqttClient
    fun getMemoryPersistence(): MqttClientPersistence
    fun getNewConnectionOptions(): MqttConnectOptions
    fun getMqttRegex(t: String): MqttRegex
    fun getRoom(dagger: Dagger?, roomType: RoomType?): Room
}

class InstanceHelperImpl : InstanceHelper {
    override fun getMqttClient(serverUri: String, clientId: String, mqttClientPersistence: MqttClientPersistence)
            : MqttClient = MqttClient(serverUri, clientId, mqttClientPersistence)

    override fun getMemoryPersistence(): MqttClientPersistence = MemoryPersistence()

    override fun getNewConnectionOptions() = MqttConnectOptions()

    override fun getMqttRegex(t: String) = MqttRegex(t)

    override fun getRoom(dagger: Dagger?, roomType: RoomType?): Room = Room(dagger, roomType)
}
