package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.MAGIC_NUMBER
import java.util.*

class WebAssemblyModule(var version: WebAssemblyVersion = WebAssemblyVersion.WASM1) {

    private val sections = LinkedList<WebAssemblySection>()

    fun generateBytes() : ByteArray {
        val bytes = ByteArray(8)
        writeMagicNumber(bytes, 0)
        writeVersion(bytes, 4)
        return bytes
    }

    private fun writeMagicNumber(byteArray: ByteArray, index: Int) {
        MAGIC_NUMBER.forEachIndexed { i, byte ->  byteArray[index + i] = byte }
    }

    private fun writeVersion(byteArray: ByteArray, index: Int) {
        version.byteArray.forEachIndexed { i, byte ->  byteArray[index + i] = byte }
    }

    fun addSection(section: WebAssemblySection) {
        sections.add(section)
    }

}
