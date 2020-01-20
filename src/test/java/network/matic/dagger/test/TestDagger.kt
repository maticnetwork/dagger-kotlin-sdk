package network.matic.dagger.test

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import network.matic.dagger.Dagger
import network.matic.dagger.Options
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID.randomUUID

@RunWith(MockitoJUnitRunner::class)
class TestDagger {

    @Mock
    lateinit var mockedOptions: Options
    private lateinit var mockedDaggerInitializedWithUrlOnly: Dagger
    private lateinit var mockedDagger: Dagger
    private val url = "tcp://ropsten.dagger.matic.network"
    private val clientId = randomUUID().toString()

    @Before
    fun setup() {
        `when`(mockedOptions.clientId).thenReturn(clientId)

        mockedDaggerInitializedWithUrlOnly = Dagger(url)
        mockedDagger = Dagger(url, mockedOptions)
    }

    @Test
    fun `should return url on getUrl call success`() {
        assertEquals(url, mockedDaggerInitializedWithUrlOnly.url)
        assertEquals(url, mockedDagger.url)
    }

    @Test
    fun `should return mockedOptions on getOptions call success when options was passed as constructor args`() {
        assertEquals(mockedOptions, mockedDagger.options)
    }

    @Test
    fun `should return new options object on getOptions call success when options was not passed as constructor args`() {
        val options = mockedDaggerInitializedWithUrlOnly.options
        with(options.mqttConnectOptions) {
            assertTrue(isCleanSession)
            assertTrue(isAutomaticReconnect)
            assertEquals(120, connectionTimeout)
        }
        assertTrue(options.clientId.isNotBlank())
        assertTrue(options.mqttClientPersistence != null)
    }
}