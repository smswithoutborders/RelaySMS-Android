package com.example.sw0b_001

import com.example.sw0b_001.data.Helpers
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.nio.ByteBuffer

class HelperTest {

    @Test
    fun extractFirstPathTest() {
        val data = "apps://site.example.com/index.html?state=1urkUIP2&code=ejV5eEYx"
        val output = Helpers.getPath(data)
        assertEquals("/index.html", output)
    }

    @Test
    fun extractParametersTest() {
        val data = "apps://site.example.com/index.html?state=1urkUIP2&code=ejV5eEYx"
        val mappedParameters = Helpers.extractParameters(data)
        val expectedMap = mapOf("state" to "1urkUIP2", "code" to "ejV5eEYx")
        assertEquals(expectedMap, mappedParameters)
    }

    @Test
    fun bytePackingTest() {
        val value = byteArrayOf(0x0, 0x1, 0x0, 0x0)
        val bytesLen: ByteArray = ByteArray(4)
        ByteBuffer.wrap(bytesLen).putInt(1)

        assertArrayEquals(bytesLen, joinByteArrays(value))
    }

    fun joinByteArrays(vararg arrays: ByteArray): ByteArray {
        // Calculate the total length of the final byte array
        val totalLength = arrays.sumOf { it.size }

        // Create a new array to hold all the bytes
        val result = ByteArray(totalLength)

        // Copy each array into the result array
        var currentIndex = 0
        for (array in arrays) {
            array.copyInto(result, currentIndex)
            currentIndex += array.size
        }

        return result
    }
}