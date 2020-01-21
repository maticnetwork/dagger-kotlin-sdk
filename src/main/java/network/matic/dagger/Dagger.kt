package network.matic.dagger

import com.google.inject.Guice
import com.google.inject.Injector
import network.matic.dagger.exceptions.DaggerException
import org.eclipse.paho.client.mqttv3.*

open class Dagger @JvmOverloads constructor(internal val url: String?,
                                            internal var options: Options = Options()) : MqttCallback {

    private val injector: Injector = Guice.createInjector(DependencyInjector())
    private var instanceHelper: InstanceHelper
    private var daggerInvocationHelper: DaggerInvocationHelper

    init {
        instanceHelper = injector.getInstance(InstanceHelper::class.java)
        daggerInvocationHelper = DaggerInvocationHelperImpl(this, url, options, instanceHelper)
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

    fun start() {
        daggerInvocationHelper.start()
    }

    fun stop() {
        daggerInvocationHelper.stop()
    }

    fun isConnected() = daggerInvocationHelper.isConnected()

    fun reconnect() {
        daggerInvocationHelper.reconnect()
    }

    @Throws(DaggerException::class)
    fun on(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.on(eventName, listener)

    @Throws(DaggerException::class)
    fun off(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.off(eventName, listener)

    @Throws(DaggerException::class)
    fun addListener(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.addListener(eventName, listener)

    @Throws(DaggerException::class)
    fun removeListener(eventName: String, listener: Listener?): Dagger = daggerInvocationHelper.removeListener(eventName, listener)

    @Throws(DaggerException::class)
    fun removeAllListeners(eventName: String) = daggerInvocationHelper.removeAllListeners(eventName)

    fun getMatchingTopics(eventName: String): List<String> = daggerInvocationHelper.getMatchingTopics(eventName)

    fun getAllSubscriptions(): MutableSet<String> = daggerInvocationHelper.getAllSubscriptions()

    @Throws(DaggerException::class)
    fun of(roomType: RoomType?): Room = daggerInvocationHelper.of(roomType)

    companion object {
        const val MESSAGE = "message"
    }
}
