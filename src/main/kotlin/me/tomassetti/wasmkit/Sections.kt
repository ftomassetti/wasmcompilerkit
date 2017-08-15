package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.sizeInBytesOfU32
import java.util.*

enum class SectionType(val id: Byte) {
    CUSTOM(0),
    TYPE(1),
    IMPORT(2),
    FUNCTION(3),
    TABLE(4),
    MEMORY(5),
    GLOBAL(6),
    EXPORT(7),
    START(8),
    ELEMENT(9),
    CODE(10),
    DATA(11);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}

sealed class WebAssemblySection(val type : SectionType) : Sized {
    abstract fun payloadSize() : Long
    // 1 for the type of the section
    // 5 for the payload of the section
    final override fun sizeInBytes() = payloadSize() + 6
}

abstract class WebAssemblyVectorSection<E:Any>(type : SectionType) : WebAssemblySection(type) {
    private val _elements = LinkedList<E>()

    val elements : List<E>
        get() = _elements.toList()

    fun addElement(element: E) {
        _elements.add(element)
    }

    fun nElements() = _elements.size

    override fun payloadSize(): Long {
        val elementsSize = elements.fold(0L, { acc, el -> acc + elementSize(el) })
        return sizeInBytesOfU32(elements.size) + elementsSize
    }

    private fun elementSize(element: Any): Long {
        return when (element) {
            is Sized -> element.sizeInBytes()
            is Long -> sizeInBytesOfU32(element)
            else -> TODO(element.javaClass.canonicalName)
        }
    }

    override fun toString(): String {
        return "WebAssemblyVectorSection(_elements=$_elements)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as WebAssemblyVectorSection<*>

        if (_elements != other._elements) return false

        return true
    }

    override fun hashCode(): Int {
        return _elements.hashCode()
    }

}

class WebAssemblyCustomSection(val name: String, val data: ByteArray) : WebAssemblySection(SectionType.CUSTOM) {
    override fun payloadSize(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class WebAssemblyTypeSection : WebAssemblyVectorSection<FuncType>(SectionType.TYPE)

class WebAssemblyImportSection : WebAssemblyVectorSection<ImportEntry>(SectionType.IMPORT)

class WebAssemblyFunctionSection : WebAssemblyVectorSection<TypeIndex>(SectionType.FUNCTION)

class WebAssemblyTableSection : WebAssemblyVectorSection<TableDefinition>(SectionType.TABLE)

class WebAssemblyMemorySection : WebAssemblyVectorSection<MemoryDefinition>(SectionType.MEMORY)

class WebAssemblyGlobalSection : WebAssemblyVectorSection<GlobalDefinition>(SectionType.GLOBAL)

class WebAssemblyExportSection : WebAssemblyVectorSection<ExportEntry>(SectionType.EXPORT)

class WebAssemblyStartSection(val startIndex: FuncIndex) : WebAssemblySection(SectionType.START) {
    override fun payloadSize(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class WebAssemblyElementSection : WebAssemblyVectorSection<ElementSegment>(SectionType.ELEMENT)

class WebAssemblyCodeSection : WebAssemblyVectorSection<CodeEntry>(SectionType.CODE)

class WebAssemblyDataSection : WebAssemblyVectorSection<DataSegment>(SectionType.DATA)
