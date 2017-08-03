package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.AdvancedBytesWriter
import me.tomassetti.wasmkit.serialization.ToBytesArrayBytesWriter
import org.junit.Assert.assertEquals
import org.junit.Test as test

class BytesWriterTest {

    @test
    fun encode624485() {
        val toBA = ToBytesArrayBytesWriter()
        val abw = AdvancedBytesWriter(toBA)
        val expectedBytes = byteArrayOf(0xE5.toByte(), 0x8E.toByte(), 0x26)
        abw.writeU32(624485L)
        val actualBytes = toBA.bytes()
        assertEquals(expectedBytes.toList(), actualBytes.toList())
    }

    @test
    fun encodeMinus624485() {
        val toBA = ToBytesArrayBytesWriter()
        val abw = AdvancedBytesWriter(toBA)
        val bytes = byteArrayOf(0x9B.toByte(), 0xF1.toByte(), 0x59)
        abw.writeS32(-624485L)
        assertEquals(bytes.toList(), toBA.bytes().toList())
    }

}
