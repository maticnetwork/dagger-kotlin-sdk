package network.matic.dagger

import network.matic.dagger.EnumHolder.TokenType
import network.matic.dagger.EnumHolder.TokenType.SINGLE


data class Token(val type: TokenType = SINGLE, val name: String = "", val piece: String = "", val last: String = "")

class EnumHolder {
    enum class TokenType {
        SINGLE, MULTI, RAW
    }
}
