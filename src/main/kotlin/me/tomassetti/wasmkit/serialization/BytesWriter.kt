package me.tomassetti.wasmkit.serialization

import java.io.ByteArrayOutputStream

interface BytesWriter {
    fun writeByte(byte: Byte)
}

class ToBytesArrayBytesWriter : BytesWriter {
    private val byteArrayOutputStream = ByteArrayOutputStream()

    override fun writeByte(byte: Byte) {
        byteArrayOutputStream.write(byteArrayOf(byte))
    }

    fun bytes() = byteArrayOutputStream.toByteArray()
}

class AdvancedBytesWriter(val basic: BytesWriter) {

    fun writeByte(byte: Byte) {
        basic.writeByte(byte)
    }

    fun writeBytes(bytes: ByteArray) {
        bytes.forEach { writeByte(it) }
    }

    fun writeU32(value: Long) {
        TODO()
    }

    fun writeS32(value: Long) {
        TODO()
    }

}