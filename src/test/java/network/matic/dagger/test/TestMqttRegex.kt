package network.matic.dagger.test

import com.google.gson.Gson
import network.matic.dagger.EnumHolder.TokenType.*
import network.matic.dagger.MqttRegex
import network.matic.dagger.Token
import network.matic.dagger.exceptions.DaggerException
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

data class Topic(
        val topic: String,
        val tokens: Array<String>,
        val regex: String,
        val matches: Map<String, Boolean>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Topic

        if (topic != other.topic) return false
        if (!tokens.contentEquals(other.tokens)) return false
        if (regex != other.regex) return false
        if (matches != other.matches) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic.hashCode()
        result = 31 * result + tokens.contentHashCode()
        result = 31 * result + matches.hashCode()
        result = 31 * result + regex.hashCode()
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
            t.matches.forEach { (key: String?, value: Boolean) -> assertEquals(String.format("Topic `%s` should%swith match with `%s`", key, if (value) " not " else " ", t.topic), mqttRegex.matches(key), value) }
        }
    }

    @Test
    fun `should return topic on getTopic call success`() {
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            assertEquals(MqttRegex.tokanize(t.topic).joinToString("/"), mqttRegex.topic)
        }
    }

    @Test
    fun `should return raw topic on getRawTopic call success`() {
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            assertEquals(t.topic.toLowerCase(), mqttRegex.rawTopic)
        }
    }

    @Test
    fun `should return regex on getRegexp call success`() {
        for (t in topics) {
            val mqttRegex = MqttRegex(t.topic)
            assertEquals(t.regex, mqttRegex.regexp.toString())
        }
    }

    @Test
    fun `should return tokens on tokanize call success`() {
        topics.forEach {
            assertEquals(it.tokens, MqttRegex.tokanize(it.topic))
        }
    }

    @Test
    @Throws(DaggerException::class)
    fun `should return regex on makeRegex call success`() {
        for (t in topics) {
            val tokens = MqttRegex.tokanize(t.topic)
            val tokenObjects = arrayOfNulls<Token>(tokens.size)
            for (index in tokens.indices) {
                tokenObjects[index] = MqttRegex.processToken(tokens[index], index, tokens)
            }
            assertEquals(t.regex, MqttRegex.makeRegex(tokenObjects).toString())
        }
    }

    @Test
    @Throws(DaggerException::class)
    fun `should return token on processToken call success`() {
        for (t in topics) {
            val tokens = MqttRegex.tokanize(t.topic)
            for (index in tokens.indices) {
                val token = tokens[index]
                val cleanToken: String = token.trim { it <= ' ' }
                val expectedToken = when {
                    cleanToken[0] == '+' -> {
                        Token(SINGLE, "", "([^/#+]+/)", "([^/#+]+/?)")
                    }
                    cleanToken[0] == '#' -> {
                        Token(MULTI, "#", "((?:[^/#+]+/)*)", "((?:[^/#+]+/?)*)")
                    }
                    else -> {
                        Token(RAW, cleanToken, String.format("%s/", cleanToken), String.format("%s/?", cleanToken))
                    }
                }
                assertEquals(expectedToken, MqttRegex.processToken(tokens[index], index, tokens))
            }
        }
    }

}