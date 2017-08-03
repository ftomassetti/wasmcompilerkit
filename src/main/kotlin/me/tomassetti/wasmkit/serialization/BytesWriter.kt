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

fun sizeInBytesOfU32(value: Int) = sizeInBytesOfU32(value.toLong())

fun sizeInBytesOfU32(value: Long) : Long{
    var res = 0L
    val counter = object: BytesWriter {
        override fun writeByte(byte: Byte) {
            res += 1
        }
    }
    AdvancedBytesWriter(counter).writeU32(value)
    return res
}

fun sizeInBytesOfS32(value: Long) : Long{
    var res = 0L
    val counter = object: BytesWriter {
        override fun writeByte(byte: Byte) {
            res += 1
        }
    }
    AdvancedBytesWriter(counter).writeS32(value)
    return res
}

class AdvancedBytesWriter(val basic: BytesWriter) {

    fun writeByte(byte: Byte) {
        basic.writeByte(byte)
    }

    fun writeBytes(bytes: ByteArray) {
        bytes.forEach { writeByte(it) }
    }

    fun writeU32(value: Long, remainingForcedSize : Int? = null) {
        // take last 7 bit
        val low7bits = value.and(0x7F)
        val rest = value.ushr(7)
        val continuation = (rest != 0L) || (remainingForcedSize != null && remainingForcedSize > 1)
        val continuationBit = if (continuation) 128 else 0
        val encodedByte = low7bits + continuationBit
        writeByte(encodedByte.toByte())
        if (continuation) {
            writeU32(rest, remainingForcedSize?.minus(1))
        }
    }

    fun writeS32(value: Long) {
        // take last 7 bit
        val low7bits = value.and(0x7F)
        val rest = value.shr(7)
        val continuationBit = if (rest == -1L) 0 else 128
        val encodedByte = low7bits + continuationBit
        writeByte(encodedByte.toByte())
        if (rest != 0-1L) {
            writeS32(rest)
        }
    }

    fun writeU32on5bytes(value: Long) {
        writeU32(value, 5)
    }

}