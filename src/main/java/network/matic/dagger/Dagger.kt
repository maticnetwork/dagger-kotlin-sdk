package network.matic.dagger

import network.matic.dagger.exceptions.DaggerException
import org.eclipse.paho.client.mqttv3.*

open class Dagger @JvmOverloads constructor(internal val url: String? = null,
                                            internal var options: Options? = null) : MqttCallback {

    private var instanceHelper: InstanceHelper
    private var daggerInvocationHelper: DaggerInvocationHelper

    init {
        instanceHelper = InstanceHelperImpl()
        daggerInvocationHelper = instanceHelper.getDaggerInvocationHelper(this, url, instanceHelper, options)
    }

    override fun connectionLost(cause: Throwable) {
        daggerInvocationHelper.connectionLost(cause)
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        daggerInvocationHelper.messageArrived(topic, message)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        daggerInvocationHelper.deliveryComplete(token)
    }

    open fun start() {
        daggerInvocationHelper.start()
    }

    open fun stop() {
        daggerInvocationHelper.stop()
    }

    open fun isConnected() = daggerInvocationHelper.isConnected()

    open fun reconnect() {
        daggerInvocationHelper.reconnect()
    }

    @Throws(DaggerException::class)
    open fun on(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.on(eventName, listener)

    @Throws(DaggerException::class)
    open fun off(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.off(eventName, listener)

    @Throws(DaggerException::class)
    open fun addListener(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.addListener(eventName, listener)

    @Throws(DaggerException::class)
    open fun removeListener(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.removeListener(eventName, listener)

    @Throws(DaggerException::class)
    open fun removeAllListeners(eventName: String) = daggerInvocationHelper.removeAllListeners(eventName)

    open fun getMatchingTopics(eventName: String): List<String> = daggerInvocationHelper.getMatchingTopics(eventName)

    open fun getAllSubscriptions(): MutableSet<String> = daggerInvocationHelper.getAllSubscriptions()

    @Throws(DaggerException::class)
    open fun of(roomType: RoomType?): Room = daggerInvocationHelper.of(roomType)

    companion object {
        const val MESSAGE = "message"
    }
}
