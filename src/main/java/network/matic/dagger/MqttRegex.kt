package network.matic.dagger

import network.matic.dagger.exceptions.DaggerException
import network.matic.dagger.utility.Numeric
import java.util.regex.Pattern

open class MqttRegex(internal val t: String) {

    internal var topic: String
    internal val rawTopic: String
    internal val regexp: Pattern

    init {
        val topic = t.toLowerCase()
        val tokens: Array<String> = tokanize(topic)
        this.topic = java.lang.String.join("/", *tokens)
        rawTopic = topic
        val tokenObjects = arrayOfNulls<Token>(tokens.size)
        for (index in tokens.indices) {
            tokenObjects[index] = processToken(tokens[index], index, tokens)
        }
        regexp = makeRegex(tokenObjects)
    }

    fun matches(rawTopic: String): Boolean {
        val topic = rawTopic.toLowerCase()
        return regexp.matcher(topic).matches()
    }

    companion object {

        fun tokanize(topic: String): Array<String> {
            var topic = topic
            topic = topic.toLowerCase()
            val tokens = topic.split("/").toTypedArray()
            if (tokens.size >= 4 && tokens[0].contains(":log")) {
                for (i in 4 until tokens.size) {
                    if (tokens[i] != "+" && tokens[i] != "#") {
                        tokens[i] = Numeric.toStringPadded(tokens[i], 64)
                    }
                }
            }
            return tokens
        }

        fun makeRegex(tokens: Array<Token?>): Pattern {
            val lastToken = tokens[tokens.size - 1]
            val result = arrayOfNulls<String>(tokens.size)
            for (index in tokens.indices) {
                val token = tokens[index]
                val isLast = index == tokens.size - 1
                val beforeMulti = index == tokens.size - 2 && lastToken?.type === EnumHolder.TokenType.MULTI
                result[index] = if (isLast || beforeMulti) token?.last else token?.piece
            }
            return Pattern.compile(String.format("^%s$", java.lang.String.join("", *result)))
        }

        @Throws(DaggerException::class)
        fun processToken(token: String?, index: Int, tokens: Array<String>): Token {
            val last = index == tokens.size - 1
            if (token == null || "" == token.trim { it <= ' ' }) {
                throw DaggerException("Topic must not be empty in pattern path.")
            }
            val cleanToken = token.trim { it <= ' ' }
            if (cleanToken[0] == '+') {
                return Token(EnumHolder.TokenType.SINGLE, "", "([^/#+]+/)", "([^/#+]+/?)")
            } else if (cleanToken[0] == '#') {
                if (!last) {
                    throw DaggerException("# wildcard must be at the end of the pattern")
                }
                return Token(EnumHolder.TokenType.MULTI, "#", "((?:[^/#+]+/)*)", "((?:[^/#+]+/?)*)")
            }
            return Token(EnumHolder.TokenType.RAW, cleanToken, String.format("%s/", cleanToken), String.format("%s/?", cleanToken))
        }
    }
}
