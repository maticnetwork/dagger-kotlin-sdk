package network.matic.dagger.test

import com.google.gson.Gson
import network.matic.dagger.MqttRegex
import network.matic.dagger.Token
import network.matic.dagger.exceptions.DaggerException
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

data class Topic(
        val topic: String,
        val tokens: Array<String>,
        val matches: Map<String, Boolean>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Topic

        if (topic != other.topic) return false
        if (!tokens.contentEquals(other.tokens)) return false
        if (matches != other.matches) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic.hashCode()
        result = 31 * result + tokens.contentHashCode()
        result = 31 * result + matches.hashCode()
        return result
    }
}

class TestMqttRegex {
    private lateinit var topics: Array<Topic>

    @Before
    @Throws(DaggerException::class, FileNotFoundException::class)
    fun setup() {
        val gson = Gson()
        val file = File(javaClass.classLoader.getResource("mqtt-regex.json").file
        )
        topics = gson.fromJson(FileReader(file), Array<Topic>::class.java)
    }

    @Test
    @Throws(DaggerException::class)
    fun `should match raw topic, tokens and complete successfully`() {
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            // check tokens
            Assert.assertArrayEquals(MqttRegex.tokanize(t.topic), t.tokens)
            // check matches
            t.matches.forEach { (key: String?, value: Boolean) -> Assert.assertEquals(String.format("Topic `%s` should%swith match with `%s`", key, if (value) " not " else " ", t.topic), mqttRegex.matches(key), value) }
        }
    }

    @Test
    fun `should return topic on getTopic call success`(){
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            assertEquals(MqttRegex.tokanize(t.topic).joinToString("/"), mqttRegex.topic)
        }
    }

    @Test
    fun `should return raw topic on getRawTopic call success`(){
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            assertEquals(t.topic.toLowerCase(), mqttRegex.rawTopic)
        }
    }

//    @Test
//    fun `should return regex on getRegexp call success`(){
//        for (t in topics) {
//            val mqttRegex = MqttRegex(t.topic)
//            val tokens = MqttRegex.tokanize(t.topic)
//            val tokenObjects = arrayOfNulls<Token>(topics.size)
//            for (index in tokens.indices) {
//                tokenObjects[index] = MqttRegex.processToken(tokens[index], index, tokens)
//                println("token object ${tokenObjects[index]}")
//            }
//            assertEquals(t.topic, MqttRegex.makeRegex(tokenObjects))
//        }
//    }

    @Test fun `should return tokens on tokanize call success`(){
        topics.forEach {
            assertEquals(it.tokens, MqttRegex.tokanize(it.topic))
        }
    }
}