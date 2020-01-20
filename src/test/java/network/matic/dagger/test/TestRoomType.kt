package network.matic.dagger.test

import network.matic.dagger.RoomType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class TestRoomType {

    private lateinit var latestRoomType  : RoomType
    private lateinit var confirmedRoomType : RoomType

    @Before
    fun setup(){
        latestRoomType = RoomType.LATEST
        confirmedRoomType = RoomType.CONFIRMED
    }

    @Test
    fun `should return room on toString call success`(){
        assertEquals("latest", latestRoomType.toString())
        assertNotEquals("confirmed", latestRoomType.toString())
        assertEquals("confirmed", confirmedRoomType.toString())
        assertNotEquals("latest", confirmedRoomType.toString())
    }

    @Test fun `should return room on getRoom call success`(){
        assertEquals("latest", latestRoomType.toString())
        assertNotEquals("confirmed", latestRoomType.toString())
        assertEquals("confirmed", confirmedRoomType.toString())
        assertNotEquals("latest", confirmedRoomType.toString())
    }
}