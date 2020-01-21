package network.matic.dagger.test

import junit.framework.Assert.assertEquals
import network.matic.dagger.Callback
import network.matic.dagger.Options
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.UUID.randomUUID

class TestOptions {

    private lateinit var callback: Callback
    private lateinit var mqttConnectOptions: MqttConnectOptions
    private lateinit var mqttClientPersistence: MqttClientPersistence
    private val clientId = randomUUID().toString()
    private lateinit var testOptions: Options

    @Before
    fun setup() {
        callback = object : Callback {
            override fun connectionLost(cause: Throwable?) {}
        }
        mqttConnectOptions = MqttConnectOptions()
        mqttClientPersistence = MemoryPersistence()
        testOptions = Options()
    }

    @Test
    fun `should set connection options on setMqttConnectOptions and return the same getMqttConnectOptionson call success`() {
        testOptions.mqttConnectOptions = mqttConnectOptions
        assertEquals(mqttConnectOptions, testOptions.mqttConnectOptions)
    }

    @Test
    fun `should set clientId on setClientId and return the same getClientId call success`() {
        testOptions.clientId = clientId
        assertEquals(clientId, testOptions.clientId)
    }

    @Test
    fun `should set persistence on setMqttClientPersistence and return the same getMqttClientPersistence call success`() {
        testOptions.mqttClientPersistence = mqttClientPersistence
        assertEquals(mqttClientPersistence, testOptions.mqttClientPersistence)
    }
}