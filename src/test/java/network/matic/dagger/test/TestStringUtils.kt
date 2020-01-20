package network.matic.dagger.test

import network.matic.dagger.Strings
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID.randomUUID

class TestStringUtils {

    @Test
    fun `should join list of strings with specified delimiter on join method call success`() {
        val list = listOf(randomUUID().toString(), randomUUID().toString(), randomUUID().toString())
        val delimiter = ":"
        val expectedResult = list.joinToString(separator = delimiter)

        val obtainedResult = Strings.join(list, delimiter)

        assertEquals(expectedResult, obtainedResult)
    }

    @Test
    fun `should capitalise first letter on capitaliseFirstLetter method call success`() {
        val inputString = randomUUID().toString()
        val inputStringInUpperCase = randomUUID().toString().toUpperCase()
        val inputStringWithFirstLetterCapitalized = inputString.substring(0, 1).toUpperCase() + inputString.substring(1)

        assertEquals(inputStringWithFirstLetterCapitalized, Strings.capitaliseFirstLetter(inputString))
        assertEquals(inputStringInUpperCase, Strings.capitaliseFirstLetter(inputStringInUpperCase))
    }

    @Test
    fun `should return unchanged on capitaliseFirstLetter method call success when input is empty or null`() {
        val inputString = ""
        val nullInputString = null
        assertEquals(inputString, Strings.capitaliseFirstLetter(inputString))
        assertEquals(nullInputString, Strings.capitaliseFirstLetter(nullInputString))
    }

    @Test
    fun `should convert first letter to lowercase on lowercaseFirstLetter method call success`() {
        val inputString = randomUUID().toString()
        val inputStringWithFirstLetterLowercased = inputString.substring(0, 1).toLowerCase() + inputString.substring(1)
        val inputStringInLowerCase = randomUUID().toString().toLowerCase()

        assertEquals(inputStringWithFirstLetterLowercased, Strings.lowercaseFirstLetter(inputString))
        assertEquals(inputStringInLowerCase, Strings.lowercaseFirstLetter(inputStringInLowerCase))
    }

    @Test
    fun `should return unchanged on lowercaseFirstLetter method call success when input is empty or null`() {
        val inputString = ""
        val nullInputString = null
        assertEquals(inputString, Strings.lowercaseFirstLetter(inputString))
        assertEquals(nullInputString, Strings.lowercaseFirstLetter(nullInputString))
    }

    @Test
    fun `should return n number of zeroes on zeros call success`() {
        val inputValue = '0'
        val numberOfRepetitions = 5
        val expectedResult = Strings.repeat(inputValue, numberOfRepetitions)

        assertEquals(expectedResult, Strings.zeros(numberOfRepetitions))
    }

    @Test
    fun `should return string with repeated characters on repeat call success`() {
        val inputValue = randomUUID().toString()[0]
        val numberOfRepetitions = 5
        val expectedResult = String(CharArray(numberOfRepetitions)).replace('\u0000', inputValue)

        assertEquals(expectedResult, Strings.repeat(inputValue, numberOfRepetitions))
    }

    @Test
    fun `should return true when input is null or empty on isEmpty call success`() {
        val emptyInput = ""
        val nullInput = null

        assertTrue(Strings.isEmpty(emptyInput))
        assertTrue(Strings.isEmpty(nullInput))
        assertFalse(Strings.isEmpty(randomUUID().toString()))
    }
}