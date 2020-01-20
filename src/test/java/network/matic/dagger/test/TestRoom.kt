package network.matic.dagger.test

import network.matic.dagger.Dagger
import network.matic.dagger.Listener
import network.matic.dagger.Room
import network.matic.dagger.RoomType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID.randomUUID


@RunWith(MockitoJUnitRunner::class)
class TestRoom {

    @Mock
    lateinit var mockedDagger: Dagger
    @Mock
    lateinit var mockedListener: Listener
    private lateinit var latestRoom: Room
    private lateinit var confirmedRoom: Room

    @Before
    fun setup() {
        latestRoom = Room(mockedDagger, RoomType.LATEST)
        confirmedRoom = Room(mockedDagger, RoomType.CONFIRMED)
    }

    @Test
    fun `should return room type on getRoomType call success`() {
        assertEquals(RoomType.LATEST, latestRoom.roomType)
        assertEquals(RoomType.CONFIRMED, confirmedRoom.roomType)
    }

    @Test
    fun `should return dagger instance on getDagger call success`() {
        assertEquals(mockedDagger, latestRoom.dagger)
        assertEquals(mockedDagger, confirmedRoom.dagger)
    }

    @Test
    fun `should start dagger when on method is called successfully`() {
        val eventName = randomUUID().toString()

        latestRoom.on(eventName, mockedListener)
        verify(mockedDagger).on("${latestRoom.roomType}:$eventName", mockedListener)

        confirmedRoom.on(eventName, mockedListener)
        verify(mockedDagger).on("${confirmedRoom.roomType}:$eventName", mockedListener)
    }

    @Test
    fun `should stop dagger when off method is called successfully`() {
        val eventName = randomUUID().toString()

        latestRoom.off(eventName, mockedListener)
        verify(mockedDagger).off("${latestRoom.roomType}:$eventName", mockedListener)

        confirmedRoom.off(eventName, mockedListener)
        verify(mockedDagger).off("${confirmedRoom.roomType}:$eventName", mockedListener)
    }
}