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

    fun typeSection() : WebAssemblyTypeSection? = _sections
            .filter { it.type == SectionType.TYPE }
            .map { it as WebAssemblyTypeSection }.firstOrNull()

    fun dataSection() : WebAssemblyDataSection? = _sections
            .filter { it.type == SectionType.DATA }
            .map { it as WebAssemblyDataSection }.firstOrNull()

    fun elementSection() : WebAssemblyElementSection? = _sections
            .filter { it.type == SectionType.ELEMENT }
            .map { it as WebAssemblyElementSection }.firstOrNull()

    fun exportSection() : WebAssemblyExportSection? = _sections
            .filter { it.type == SectionType.EXPORT }
            .map { it as WebAssemblyExportSection }.firstOrNull()

    fun importSection() : WebAssemblyImportSection? = _sections
            .filter { it.type == SectionType.IMPORT }
            .map { it as WebAssemblyImportSection }.firstOrNull()

    fun tableSection() : WebAssemblyTableSection? = _sections
            .filter { it.type == SectionType.TABLE }
            .map { it as WebAssemblyTableSection }.firstOrNull()

    fun globalSection() : WebAssemblyGlobalSection? = _sections
            .filter { it.type == SectionType.GLOBAL }
            .map { it as WebAssemblyGlobalSection }.firstOrNull()

    fun memorySection() : WebAssemblyMemorySection? = _sections
            .filter { it.type == SectionType.MEMORY }
            .map { it as WebAssemblyMemorySection }.firstOrNull()

    fun startSection() : WebAssemblyStartSection? = _sections
            .filter { it.type == SectionType.START }
            .map { it as WebAssemblyStartSection }.firstOrNull()

    fun isValid() : Boolean {
        val nCode = codeSection()?.nElements() ?: 0
        val nFunction = functionSection()?.nElements() ?: 0
        return nCode == nFunction
    }

    fun  sectionsByType(sectionType: SectionType): List<WebAssemblySection> = sections.filter { it.type == sectionType }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as WebAssemblyModule

        if (version != other.version) return false
        if (_sections != other._sections) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + _sections.hashCode()
        return result
    }

    override fun toString(): String {
        return "WebAssemblyModule(version=$version, _sections=$_sections)"
    }

}
