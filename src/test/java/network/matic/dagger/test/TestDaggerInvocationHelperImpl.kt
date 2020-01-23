package network.matic.dagger.test

import junit.framework.Assert.*
import network.matic.dagger.*
import network.matic.dagger.exceptions.DaggerException
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID.randomUUID

@RunWith(MockitoJUnitRunner::class)
class TestDaggerInvocationHelperImpl {

    @Mock
    lateinit var dagger: Dagger
    @Mock
    lateinit var instanceHelper: InstanceHelper
    @Mock
    lateinit var callback: Callback
    @Mock
    lateinit var mqttClient: MqttClient
    @Mock
    lateinit var iMqttToken: IMqttToken
    @Mock
    lateinit var listener: Listener
    @Mock
    lateinit var mqttRegex: MqttRegex
    var options: Options = Options()
    private val ropstenUrl = "tcp://ropsten.dagger.matic.network"
    private val clientId = randomUUID().toString()
    private val mqttConnectOptions = MqttConnectOptions()
    private val persistence = MemoryPersistence()
    private val regexTopicsMap: MutableMap<String, MqttRegex?> = hashMapOf()
    private val listenersMap: MutableMap<String?, MutableList<Listener>?>? = hashMapOf()
    private val listenersList: MutableList<Listener> = mutableListOf()
    private val matchingTopicsList: MutableList<String> = mutableListOf()

    @Before
    fun setup() {
        `when`(instanceHelper.getEmptyRegexTopicsMap()).thenReturn((regexTopicsMap))
        `when`(instanceHelper.getEmptyListenersMap()).thenReturn(listenersMap)
    }

    @Test(expected = DaggerException::class)
    fun `should throw dagger exception when provided url is null`() {
        DaggerInvocationHelperImpl(dagger, null, instanceHelper, options)
    }

    @Test
    fun `should create default options when options is null`() {
        options.clientId = clientId
        options.mqttClientPersistence = null
        options.mqttConnectOptions = null
        `when`(instanceHelper.getOptions()).thenReturn(options)
        `when`(instanceHelper.getMemoryPersistence()).thenReturn(persistence)
        `when`(instanceHelper.getNewConnectionOptions()).thenReturn(mqttConnectOptions)
        `when`(instanceHelper.getMqttClient(ropstenUrl, clientId, persistence)).thenReturn(mqttClient)

        DaggerInvocationHelperImpl(dagger, ropstenUrl, instanceHelper, null)

        assertTrue(mqttConnectOptions.isCleanSession)
        assertTrue(mqttConnectOptions.isAutomaticReconnect)
        assertEquals(120, mqttConnectOptions.connectionTimeout)
        assertEquals(mqttConnectOptions, options.mqttConnectOptions)
        verify(instanceHelper).getOptions()
        verify(instanceHelper).getMemoryPersistence()
        verify(instanceHelper).getNewConnectionOptions()
        verify(instanceHelper).getMqttClient(ropstenUrl, clientId, persistence)
        verify(instanceHelper).getEmptyRegexTopicsMap()
        verify(instanceHelper).getEmptyListenersMap()
    }

    @Test
    fun `should notify call back when connection is lost`() {
        val exception = Exception()
        options.clientId = clientId
        options.callback = callback
        options.mqttClientPersistence = persistence
        options.mqttConnectOptions = mqttConnectOptions

        DaggerInvocationHelperImpl(dagger, ropstenUrl, instanceHelper, options).connectionLost(exception)

        verify(callback).connectionLost(exception)
        verify(instanceHelper).getEmptyRegexTopicsMap()
        verify(instanceHelper).getEmptyListenersMap()
        verify(instanceHelper).getMqttClient(ropstenUrl, clientId, persistence)
        verify(instanceHelper).getEmptyRegexTopicsMap()
        verify(instanceHelper).getEmptyListenersMap()
    }

    @Test
    fun `should emit topic and message on messageArrived call success when listener list is empty`() {
        val topic = randomUUID().toString()
        val message = MqttMessage()
        val invocationHelper = setupDaggerInvocationHelper()
        options.clientId = clientId
        options.callback = callback
        options.mqttClientPersistence = persistence
        options.mqttConnectOptions = mqttConnectOptions
        regexTopicsMap[topic] = mqttRegex
        mqttRegex.topic = topic
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)
        `when`(instanceHelper.getEmptyMatchingTopicsList()).thenReturn(matchingTopicsList)
        `when`(mqttRegex.matches(mqttRegex.topic)).thenReturn(true)

        invocationHelper.messageArrived(topic, message)

        assertTrue(listenersMap!!.containsKey(Dagger.MESSAGE))
        verify(instanceHelper).getEmptyMatchingTopicsList()
        verify(mqttRegex, times(1)).matches(mqttRegex.topic)
        verify(instanceHelper, times(2)).getEmptyListenersList()
    }

    @Test
    fun `should emit topic and message on messageArrived call success when listeners are present`() {
        val topic = randomUUID().toString()
        val message = MqttMessage()
        val invocationHelper = setupDaggerInvocationHelper()
        options.clientId = clientId
        options.callback = callback
        options.mqttClientPersistence = persistence
        options.mqttConnectOptions = mqttConnectOptions
        regexTopicsMap[topic] = mqttRegex
        listenersMap!![Dagger.MESSAGE] = listenersList
        mqttRegex.topic = topic
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)
        `when`(instanceHelper.getEmptyMatchingTopicsList()).thenReturn(matchingTopicsList)
        `when`(mqttRegex.matches(mqttRegex.topic)).thenReturn(true)

        invocationHelper.messageArrived(topic, message)

        assertTrue(listenersMap.containsKey(Dagger.MESSAGE))
        verify(instanceHelper).getEmptyMatchingTopicsList()
        verify(mqttRegex, times(1)).matches(mqttRegex.topic)
        verify(instanceHelper).getEmptyListenersList()
    }

    @Test
    fun `should connect with result on start call success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        `when`(mqttClient.connectWithResult(options.mqttConnectOptions)).thenReturn(iMqttToken)

        invocationHelper.start()

        verify(mqttClient).setCallback(dagger)
        verify(mqttClient).connectWithResult(mqttConnectOptions)
        verify(iMqttToken).waitForCompletion()
    }

    @Test
    fun `should throw dagger exception on start call failure`() {
        val invocationHelper = setupDaggerInvocationHelper()
        `when`(mqttClient.connectWithResult(options.mqttConnectOptions)).thenReturn(iMqttToken)
        `when`(iMqttToken.waitForCompletion()).thenThrow(MqttException(0))

        try {
            invocationHelper.start()
        } catch (exception: DaggerException) {
            verify(mqttClient).setCallback(dagger)
            verify(mqttClient).connectWithResult(options.mqttConnectOptions)
            verify(iMqttToken).waitForCompletion()
        }
    }

    @Test
    fun `should disconnect on stop call success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        invocationHelper.stop()
        verify(mqttClient).disconnect()
    }

    @Test
    fun `should throw dagger exception on stop call failure`() {
        val invocationHelper = setupDaggerInvocationHelper()
        `when`(mqttClient.disconnect()).thenThrow(MqttException(0))
        try {
            invocationHelper.stop()
        } catch (e: DaggerException) {
            verify(mqttClient).disconnect()
        }
    }

    @Test
    fun `should return connection status on isConnected call success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        `when`(mqttClient.isConnected).thenReturn(false)
        assertFalse(invocationHelper.isConnected())

        `when`(mqttClient.isConnected).thenReturn(true)
        assertTrue(invocationHelper.isConnected())

        verify(mqttClient, times(2)).isConnected
    }

    @Test
    fun `should reconnect on reconnect call success`() {
        val invocationHelper = setupDaggerInvocationHelper()

        invocationHelper.reconnect()

        verify(mqttClient).reconnect()
    }

    @Test
    fun `should throw dagger exception on reconnect call failure`() {
        val invocationHelper = setupDaggerInvocationHelper()
        `when`(mqttClient.reconnect()).thenThrow(MqttException(0))
        try {
            invocationHelper.reconnect()
        } catch (e: DaggerException) {
            verify(mqttClient).reconnect()
        }
    }

    @Test
    fun `should add listener when on is called successfully and listener list is empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)

        invocationHelper.on(eventName, listener)

        assertEquals(mqttRegex, regexTopicsMap[mqttRegex.topic])
        assertTrue(listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
        verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
        verify(iMqttToken).waitForCompletion()
    }

    @Test
    fun `should add listener when on is called successfully and listener list contains event`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        listenersMap!![eventName] = listenersList
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)

        invocationHelper.on(eventName, listener)

        assertEquals(mqttRegex, regexTopicsMap[mqttRegex.topic])
        assertTrue(listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
        verify(iMqttToken).waitForCompletion()
    }

    @Test
    fun `should return dagger exception when on call is not successful`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)
        `when`(iMqttToken.waitForCompletion()).thenThrow(MqttException(0))

        try {
            invocationHelper.on(eventName, listener)
        } catch (e: DaggerException) {
            verify(instanceHelper).getMqttRegex(eventName)
            verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
            verify(iMqttToken).waitForCompletion()
        }
    }

    @Test
    fun `should remove listener and unsubscribe client when off is called successfully and event listener is empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)

        invocationHelper.off(eventName, listener)

        assertTrue(!regexTopicsMap.containsKey(mqttRegex.topic))
        assertTrue(!listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
        verify(mqttClient).unsubscribe(mqttRegex.topic)
    }

    @Test
    fun `should remove listener when off is called successfully and event listener is not empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        listenersList.add(listener)
        listenersMap!![eventName] = listenersList
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)

        invocationHelper.off(eventName, listener)

        assertTrue(!listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
    }

    @Test
    fun `should throw dagger exception when off is not called successfully`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)
        `when`(mqttClient.unsubscribe(mqttRegex.topic)).thenThrow(MqttException(0))

        try {
            invocationHelper.off(eventName, listener)
        } catch (e: DaggerException) {
            verify(instanceHelper).getMqttRegex(eventName)
            verify(instanceHelper).getEmptyListenersList()
            verify(mqttClient).unsubscribe(mqttRegex.topic)
        }
    }

    @Test
    fun `should add listener when addListener is called successfully and listener list is empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)

        invocationHelper.addListener(eventName, listener)

        assertEquals(mqttRegex, regexTopicsMap[mqttRegex.topic])
        assertTrue(listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
        verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
        verify(iMqttToken).waitForCompletion()
    }

    @Test
    fun `should add listener when addListener is called successfully and listener list contains event`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        listenersMap!![eventName] = listenersList
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)

        invocationHelper.addListener(eventName, listener)

        assertEquals(mqttRegex, regexTopicsMap[mqttRegex.topic])
        assertTrue(listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
        verify(iMqttToken).waitForCompletion()
    }

    @Test
    fun `should return dagger exception when addListener call is not successful`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.subscribeWithResponse(mqttRegex.topic)).thenReturn(iMqttToken)
        `when`(iMqttToken.waitForCompletion()).thenThrow(MqttException(0))

        try {
            invocationHelper.addListener(eventName, listener)
        } catch (e: DaggerException) {
            verify(instanceHelper).getMqttRegex(eventName)
            verify(mqttClient).subscribeWithResponse(mqttRegex.topic)
            verify(iMqttToken).waitForCompletion()
        }
    }

    @Test
    fun `should remove listener and unsubscribe client when removeListener is called successfully and event listener is empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)

        invocationHelper.removeListener(eventName, listener)

        assertTrue(!regexTopicsMap.containsKey(mqttRegex.topic))
        assertTrue(!listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
        verify(mqttClient).unsubscribe(mqttRegex.topic)
    }

    @Test
    fun `should remove listener when removeListener is called successfully and event listener is not empty`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        listenersList.add(listener)
        listenersMap!![eventName] = listenersList
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)

        invocationHelper.removeListener(eventName, listener)

        assertTrue(!listenersList.contains(listener))
        verify(instanceHelper).getMqttRegex(eventName)
    }

    @Test
    fun `should throw dagger exception when removeListener is not called successfully`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(instanceHelper.getEmptyListenersList()).thenReturn(listenersList)
        `when`(mqttClient.unsubscribe(mqttRegex.topic)).thenThrow(MqttException(0))

        try {
            invocationHelper.removeListener(eventName, listener)
        } catch (e: DaggerException) {
            verify(instanceHelper).getMqttRegex(eventName)
            verify(instanceHelper).getEmptyListenersList()
            verify(mqttClient).unsubscribe(mqttRegex.topic)
        }
    }

    @Test
    fun `should remove all listeners on removeAllListeners call success and topics map contains topic`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        regexTopicsMap[mqttRegex.topic] = mqttRegex
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)

        invocationHelper.removeAllListeners(eventName)

        assertTrue(!regexTopicsMap.containsKey(mqttRegex.topic))
        assertTrue(listenersMap!![eventName]!!.isEmpty())
        verify(mqttClient).unsubscribe(mqttRegex.topic)
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
    }

    @Test
    fun `should remove all listeners on removeAllListeners call success and topics map does not contain topic`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)

        invocationHelper.removeAllListeners(eventName)

        assertTrue(!regexTopicsMap.containsKey(mqttRegex.topic))
        assertTrue(listenersMap!![eventName]!!.isEmpty())
        verify(mqttClient).unsubscribe(mqttRegex.topic)
        verify(instanceHelper).getMqttRegex(eventName)
        verify(instanceHelper).getEmptyListenersList()
    }

    @Test
    fun `should throw dagger exception on removeAllListeners call failure`() {
        val eventName = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        mqttRegex.topic = eventName
        `when`(instanceHelper.getMqttRegex(eventName)).thenReturn(mqttRegex)
        `when`(mqttClient.unsubscribe(mqttRegex.topic)).thenThrow(MqttException(0))

        try {
            invocationHelper.removeAllListeners(eventName)
        } catch (e: DaggerException) {
            verify(mqttClient).unsubscribe(mqttRegex.topic)
            verify(instanceHelper).getMqttRegex(eventName)
        }
    }

    @Test
    fun `should return matching topics on getMatchingTopics call success`() {
        val eventName = randomUUID().toString()
        val eventName2 = randomUUID().toString()
        val invocationHelper = setupDaggerInvocationHelper()
        regexTopicsMap[eventName] = mqttRegex
        regexTopicsMap[eventName2] = mqttRegex
        mqttRegex.topic = eventName
        `when`(instanceHelper.getEmptyMatchingTopicsList()).thenReturn(matchingTopicsList)
        `when`(mqttRegex.matches(mqttRegex.topic)).thenReturn(true)

        val result = invocationHelper.getMatchingTopics(eventName)

        assertTrue(result.contains(mqttRegex.topic))
        assertFalse(result.contains(eventName2))
        verify(instanceHelper).getEmptyMatchingTopicsList()
        verify(mqttRegex, times(2)).matches(mqttRegex.topic)
    }

    @Test
    fun `should return all subscriptions on getAllSubscriptions call success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        val event1 = randomUUID().toString()
        val event2 = randomUUID().toString()
        val event3 = randomUUID().toString()
        regexTopicsMap[event1] = mqttRegex
        regexTopicsMap[event2] = mqttRegex

        val subscriptions = invocationHelper.getAllSubscriptions()
        assertTrue(subscriptions.contains(event1))
        assertTrue(subscriptions.contains(event2))
        assertTrue(!subscriptions.contains(event3))
    }

    @Test
    fun `should return dagger instance on getDaggerInstance call success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        assertEquals(dagger, invocationHelper.getDaggerInstance())
    }

    @Test
    fun `should return room when of method call is success`() {
        val invocationHelper = setupDaggerInvocationHelper()
        val confirmedRoom = Room(dagger, RoomType.CONFIRMED)
        val latestRoom = Room(dagger, RoomType.LATEST)
        `when`(instanceHelper.getRoom(dagger, RoomType.CONFIRMED)).thenReturn(confirmedRoom)
        `when`(instanceHelper.getRoom(dagger, RoomType.LATEST)).thenReturn(latestRoom)

        assertEquals(confirmedRoom, invocationHelper.of(RoomType.CONFIRMED))
        assertEquals(latestRoom, invocationHelper.of(RoomType.LATEST))
        verify(instanceHelper).getRoom(dagger, RoomType.CONFIRMED)
        verify(instanceHelper).getRoom(dagger, RoomType.LATEST)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(dagger, instanceHelper, callback, mqttClient, iMqttToken, listener, mqttRegex)
    }

    private fun setupDaggerInvocationHelper(): DaggerInvocationHelperImpl {
        options.clientId = clientId
        options.mqttClientPersistence = persistence
        options.mqttConnectOptions = mqttConnectOptions
        `when`(instanceHelper.getMqttClient(ropstenUrl, clientId, persistence)).thenReturn(mqttClient)

        val invocationHelper = DaggerInvocationHelperImpl(dagger, ropstenUrl, instanceHelper, options)

        verify(instanceHelper).getMqttClient(ropstenUrl, clientId, persistence)
        verify(instanceHelper).getEmptyRegexTopicsMap()
        verify(instanceHelper).getEmptyListenersMap()
        return invocationHelper
    }
}
