package me.tomassetti.wasmkit.serialization

interface BytesWriter {
    fun writeByte(byte: Byte)
}

class AdvancedBytesWriter(val basic: BytesWriter) {

    fun writeByte(byte: Byte) {
        basic.writeByte(byte)
    }

    fun writeBytes(bytes: ByteArray) {
        bytes.forEach { writeByte(it) }
    }

}