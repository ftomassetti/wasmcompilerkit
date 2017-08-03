package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.sizeInBytesOfU32

typealias TableIndex = Long
typealias TypeIndex = Long
typealias FuncIndex = Long
typealias MemIndex = Long

enum class WebAssemblyVersion(val byteArray: ByteArray) {
    WASM1(byteArrayOf(0x01, 0x00, 0x00, 0x00))
}

enum class ValueType(val id: Byte) {
    I32(0x7F),
    I64(0x7E),
    F32(0x7D),
    F64(0x7C);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}

data class FuncType(val paramTypes: List<ValueType>, val returnTypes: List<ValueType>) : Sized {
    override fun sizeInBytes(): Long {
        return 1 + sizeInBytesOfU32(paramTypes.size) + paramTypes.size + sizeInBytesOfU32(returnTypes.size) + returnTypes.size
    }

}

data class Limits(val min: Long, val max: Long? = null) : Sized {
    override fun sizeInBytes(): Long {
        return 1 + sizeInBytesOfU32(min) + if (max == null) 0 else sizeInBytesOfU32(max)
    }
}

interface Sized {
    fun sizeInBytes() : Long
}
