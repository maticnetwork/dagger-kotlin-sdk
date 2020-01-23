package network.matic.dagger

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

interface InstanceHelper {
    fun getOptions(): Options
    fun getMqttClient(serverUri: String, clientId: String, mqttClientPersistence: MqttClientPersistence): MqttClient
    fun getMemoryPersistence(): MqttClientPersistence
    fun getNewConnectionOptions(): MqttConnectOptions
    fun getMqttRegex(t: String): MqttRegex
    fun getRoom(dagger: Dagger?, roomType: RoomType?): Room
    fun getDaggerInvocationHelper(dagger: Dagger, url: String?, instanceHelper: InstanceHelper, options: Options?): DaggerInvocationHelper
    fun getEmptyRegexTopicsMap(): MutableMap<String, MqttRegex?>
    fun getEmptyListenersMap(): MutableMap<String?, MutableList<Listener>?>?
    fun getEmptyListenersList(): MutableList<Listener>
    fun getEmptyMatchingTopicsList(): MutableList<String>
}

class InstanceHelperImpl : InstanceHelper {
    override fun getOptions(): Options = Options()

    override fun getMqttClient(serverUri: String, clientId: String, mqttClientPersistence: MqttClientPersistence)
            : MqttClient = MqttClient(serverUri, clientId, mqttClientPersistence)

    override fun getMemoryPersistence(): MqttClientPersistence = MemoryPersistence()

    override fun getNewConnectionOptions() = MqttConnectOptions()

    override fun getMqttRegex(t: String) = MqttRegex(t)

    override fun getRoom(dagger: Dagger?, roomType: RoomType?): Room = Room(dagger, roomType)

    override fun getDaggerInvocationHelper(dagger: Dagger, url: String?, instanceHelper: InstanceHelper, options: Options?)
            : DaggerInvocationHelper = DaggerInvocationHelperImpl(dagger, url, instanceHelper, options)

    override fun getEmptyRegexTopicsMap(): MutableMap<String, MqttRegex?> = HashMap()

    override fun getEmptyListenersMap(): MutableMap<String?, MutableList<Listener>?>? = HashMap()

    override fun getEmptyListenersList(): MutableList<Listener> = mutableListOf()

    override fun getEmptyMatchingTopicsList(): MutableList<String> = mutableListOf()
}
