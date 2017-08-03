package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.MAGIC_NUMBER
import java.util.*

class WebAssemblyModule(var version: WebAssemblyVersion = WebAssemblyVersion.WASM1) {

    private val _sections = LinkedList<WebAssemblySection>()

    val sections : List<WebAssemblySection>
        get() = _sections.toList()

    private fun writeMagicNumber(byteArray: ByteArray, index: Int) {
        MAGIC_NUMBER.forEachIndexed { i, byte ->  byteArray[index + i] = byte }
    }

    private fun writeVersion(byteArray: ByteArray, index: Int) {
        version.byteArray.forEachIndexed { i, byte ->  byteArray[index + i] = byte }
    }

    fun addSection(section: WebAssemblySection) {
        if (section.type != SectionType.CUSTOM) {
            val lastType = _sections.filter { it.type != SectionType.CUSTOM }.map { it.type.id }.lastOrNull() ?: 0
            if (section.type.id <= lastType) {
                throw IllegalArgumentException("We can have at most one section per type and they must be inserted in order")
            }
        }
        _sections.add(section)
    }

    fun codeSection() : WebAssemblyCodeSection? = _sections
            .filter { it.type == SectionType.CODE }
            .map { it as WebAssemblyCodeSection }.firstOrNull()

    fun functionSection() : WebAssemblyFunctionSection? = _sections
            .filter { it.type == SectionType.FUNCTION }
            .map { it as WebAssemblyFunctionSection }.firstOrNull()

    fun isValid() : Boolean {
        val nCode = codeSection()?.nElements() ?: 0
        val nFunction = functionSection()?.nElements() ?: 0
        return nCode == nFunction
    }

}
