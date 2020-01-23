package network.matic.dagger

import network.matic.dagger.exceptions.DaggerException
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import java.util.function.Consumer

interface DaggerInvocationHelper {
    fun connectionLost(cause: Throwable)
    fun messageArrived(topic: String, message: MqttMessage)
    fun deliveryComplete(token: IMqttDeliveryToken)
    fun start()
    fun stop()
    fun isConnected(): Boolean
    fun reconnect()
    fun on(eventName: String, listener: Listener?): Dagger
    fun off(eventName: String, listener: Listener?): Dagger
    fun addListener(eventName: String, listener: Listener?): Dagger
    fun removeListener(eventName: String, listener: Listener?): Dagger
    fun removeAllListeners(eventName: String)
    fun getMatchingTopics(eventName: String): List<String>
    fun getAllSubscriptions(): MutableSet<String>
    fun of(roomType: RoomType?): Room
    fun getDaggerInstance(): Dagger
}

class DaggerInvocationHelperImpl(private val daggerInstance: Dagger,
                                 private val url: String?,
                                 private var instanceHelper: InstanceHelper,
                                 private var options: Options?) : DaggerInvocationHelper {

    private var client: MqttClient? = null
    private val regexTopics: MutableMap<String, MqttRegex?>
    private var listeners: MutableMap<String?, MutableList<Listener>?>?

    init {
        if (url.isNullOrBlank()) {
            throw DaggerException("Invalid URL")
        }
        if (options == null) {
            options = instanceHelper.getOptions()
        }
        // mqtt connection options
        if (options!!.mqttConnectOptions == null) {
            val mqttConnectOptions = instanceHelper.getNewConnectionOptions()
            mqttConnectOptions.isCleanSession = true
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.connectionTimeout = 120
            options!!.mqttConnectOptions = mqttConnectOptions
        }
        // set client id
        if (Strings.isEmpty(options!!.clientId)) {
            val clientId = UUID.randomUUID()
            options!!.clientId = clientId.toString()
        }
        // set memory persistance
        if (options!!.mqttClientPersistence == null) {
            options!!.mqttClientPersistence = instanceHelper.getMemoryPersistence()
        }
        regexTopics = instanceHelper.getEmptyRegexTopicsMap()
        listeners = instanceHelper.getEmptyListenersMap()
        try {
            client = instanceHelper.getMqttClient(this.url, options!!.clientId!!, options!!.mqttClientPersistence!!)
        } catch (e: MqttException) {
            throw DaggerException(e.message)
        }
    }

    override fun connectionLost(cause: Throwable) {
        val callback = options!!.callback
        callback?.connectionLost(cause)
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        onMessage(topic, message)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        // TODO Auto-generated method stub
    }

    @Throws(DaggerException::class)
    override fun start() {
        try {
            client!!.setCallback(daggerInstance)
            val token = client!!.connectWithResult(options!!.mqttConnectOptions)
            token?.waitForCompletion()
        } catch (e: MqttException) {
            throw DaggerException(e.message)
        }
    }

    @Throws(DaggerException::class)
    override fun stop() {
        try {
            client!!.disconnect()
        } catch (e: MqttException) {
            throw DaggerException(e.message)
        }
    }

    override fun isConnected() = client!!.isConnected

    @Throws(DaggerException::class)
    override fun reconnect() {
        try {
            client!!.reconnect()
        } catch (e: MqttException) {
            throw DaggerException(e.message)
        }
    }

    @Throws(DaggerException::class)
    override fun on(eventName: String, listener: Listener?): Dagger {
        return addListener(eventName, listener)
    }

    @Throws(DaggerException::class)
    override fun off(eventName: String, listener: Listener?): Dagger {
        return removeListener(eventName, listener)
    }

    @Throws(DaggerException::class)
    override fun addListener(eventName: String, listener: Listener?): Dagger {
        val mqttRegex = instanceHelper.getMqttRegex(eventName)
        if (!regexTopics.containsKey(mqttRegex.topic)) { // subscribe events from server using topic
            try {
                val token = client?.subscribeWithResponse(mqttRegex.topic)
                token?.waitForCompletion()
            } catch (e: MqttException) {
                throw DaggerException(e.message)
            }
            // add MQTT regex into regex topics
            regexTopics[mqttRegex.topic] = mqttRegex
        }
        val list = getEventListeners(eventName)
        if (listener != null) {
            list.add(listener)
        }
        return daggerInstance
    }

    @Throws(DaggerException::class)
    override fun removeListener(eventName: String, listener: Listener?): Dagger {
        val mqttRegex = instanceHelper.getMqttRegex(eventName)
        // if listener count is zero, unsubscribe topic and delete from `_regexTopics`
        if (getEventListeners(eventName).size == 0) { // unsubscribe events from server
            try {
                client?.unsubscribe(mqttRegex.topic)
            } catch (e: MqttException) {
                throw DaggerException(e.message)
            }
            // remove MQTT regex from regex topics
            if (regexTopics.containsKey(mqttRegex.topic)) {
                regexTopics.remove(mqttRegex.topic)
            }
        }
        val list: MutableList<Listener>? = getEventListeners(eventName)
        list?.remove(listener)
        return daggerInstance
    }

    @Throws(DaggerException::class)
    override fun removeAllListeners(eventName: String) {
        val mqttRegex = instanceHelper.getMqttRegex(eventName)
        try {
            client!!.unsubscribe(mqttRegex.topic)
        } catch (e: MqttException) {
            throw DaggerException(e.message)
        }
        if (regexTopics.containsKey(mqttRegex.topic)) {
            regexTopics.remove(mqttRegex.topic)
        }
        listeners!![eventName] = instanceHelper.getEmptyListenersList()
    }

    override fun getMatchingTopics(eventName: String): List<String> {
        val result: MutableList<String> = instanceHelper.getEmptyMatchingTopicsList()
        for ((_, mqttRegex) in regexTopics) {
            if (mqttRegex!!.matches(eventName)) {
                result.add(mqttRegex.topic)
            }
        }
        return result
    }

    override fun getAllSubscriptions() = regexTopics.keys

    override fun getDaggerInstance() = daggerInstance

    // Get room instance
    @Throws(DaggerException::class)
    override fun of(roomType: RoomType?): Room {
        return instanceHelper.getRoom(daggerInstance, roomType)
    }

    private fun onMessage(topic: String, message: MqttMessage) { // emit any message
        emit(Dagger.MESSAGE, message.payload)
        // emit events to matching listeners
        getMatchingTopics(topic).forEach {
            emit(it, message.payload)
        }
    }

    private fun emit(eventName: String, payload: ByteArray) { // execute callback in all events
        getEventListeners(eventName).forEach {
            it.callback(eventName, payload)
        }
    }

    // Get all event listeners
    private fun getEventListeners(eventName: String?): MutableList<Listener> {
        if (listeners == null) {
            listeners = instanceHelper.getEmptyListenersMap()
        }
        if (!listeners!!.containsKey(eventName)) {
            listeners!![eventName] = instanceHelper.getEmptyListenersList()
        }
        return listeners!![eventName]!!
    }

}
