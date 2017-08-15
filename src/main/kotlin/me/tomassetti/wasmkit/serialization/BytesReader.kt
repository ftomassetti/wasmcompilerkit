package me.tomassetti.wasmkit.serialization

import java.nio.ByteBuffer
import kotlin.experimental.and

class BytesReader(val bytes: ByteArray) {
    private var currentIndex = 0

    fun readNextByte() : Byte {
        if (hasFinished()) {
            throw IllegalStateException("No more bytes to read")
        }
        return bytes[currentIndex++]
    }

    fun peekNextByte() : Byte {
        if (hasFinished()) {
            throw IllegalStateException("No more bytes to read")
        }
        return bytes[currentIndex]
    }

    fun hasFinished() = remainingBytes() == 0

    fun remainingBytes() = bytes.size - currentIndex

    fun readU32() : Long {
        val byte = readNextByte()
        return byte.and(0x7F).toLong() + if (byte.and(0x80.toByte()) != 0.toByte()) 128 * readU32() else 0
    }

    fun readDouble() : Double {
        val bytes = bytes.copyOfRange(currentIndex, currentIndex + 8)
        currentIndex += 8
        return ByteBuffer.wrap(bytes).double
    }

    fun readFloat() : Float {
        val bytes = bytes.copyOfRange(currentIndex, currentIndex + 4)
        currentIndex += 4
        return ByteBuffer.wrap(bytes).float
    }

    fun readBytes(n: Long) : ByteArray = 1L.rangeTo(n).map { readNextByte() }.toByteArray()

    fun currentIndex() = currentIndex
    fun readS32(): Long {
        var result : Long = 0
        var cur: Long
        var count = 0
        var signBits : Long = -1

        do {
            cur = readNextByte().toLong().and(0xff)
            result = result or (cur and 0x7f shl count * 7)
            signBits = signBits shl 7
            count++
        } while (cur and 0x80 == 0x80L && count < 5)

        if (cur and 0x80 == 0x80L) {
            throw RuntimeException("invalid LEB128 sequence")
        }

        // Sign extend if appropriate
        if (signBits shr 1 and result != 0L) {
            result = result or signBits
        }

        return result
    }

    fun expectByte(expectedByte: Byte) {
        val readByte = readNextByte()
        if (readByte != expectedByte) {
            throw RuntimeException("Invalid byte found. Expected $expectedByte found $readByte")
        }
    }

}
