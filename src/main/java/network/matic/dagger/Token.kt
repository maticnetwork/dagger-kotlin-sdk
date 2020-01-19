package network.matic.dagger

import network.matic.dagger.TokenType.SINGLE

data class Token(val type: TokenType = SINGLE, val name: String = "", val piece: String = "", val last: String = "")

enum class TokenType {
    SINGLE, MULTI, RAW
}