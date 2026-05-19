package com.example.sw0b_001

import junit.framework.TestCase
import org.junit.Test
import uniffi.relaysms_spec_payload.addRust

class RustTest {

    companion object {
        init {
            System.loadLibrary("relaysms_spec_payload")
        }
    }

    @Test
    fun rustOutputs() {
        val output = addRust(1UL, 1UL)
        TestCase.assertEquals(2UL, output)
    }
}