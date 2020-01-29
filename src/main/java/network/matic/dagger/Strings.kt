package network.matic.dagger

object Strings {
    fun join(src: List<String>?, delimiter: String?): String? {
        return if (src == null) null else {
            val builder = StringBuilder()
            var i = 0
            val length = src.size
            while (i < length) {
                if (i > 0) builder.append(delimiter)
                builder.append(src[i])
                i++
            }
            builder.toString()
        }
    }

    fun capitaliseFirstLetter(string: String?): String? {
        return if (string == null || string.isEmpty()) {
            string
        } else {
            string.substring(0, 1).toUpperCase() + string.substring(1)
        }
    }

    fun lowercaseFirstLetter(string: String?): String? {
        return if (string == null || string.isEmpty()) {
            string
        } else {
            string.substring(0, 1).toLowerCase() + string.substring(1)
        }
    }

    fun zeros(n: Int): String {
        return repeat('0', n)
    }

    fun repeat(value: Char, n: Int): String {
        return String(CharArray(n)).replace("\u0000", value.toString())
    }

    fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty()
    }
}
