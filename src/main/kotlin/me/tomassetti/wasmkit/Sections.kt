package me.tomassetti.wasmkit

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

abstract class WebAssemblySection(val type : SectionType) {
    fun sizeInBytes() : Long = TODO()
}

abstract class WebAssemblyVectorSection<E>(type : SectionType) : WebAssemblySection(type) {
    private val _elements = LinkedList<E>()

    val elements : List<E>
        get() = _elements.toList()

    fun addElement(element: E) {
        _elements.add(element)
    }

    fun nElements() = _elements.size
}

class WebAssemblyCustomSection(val name: String, val data: ByteArray) : WebAssemblySection(SectionType.CUSTOM)

class WebAssemblyTypeSection : WebAssemblyVectorSection<FuncType>(SectionType.TYPE)

class WebAssemblyImportSection : WebAssemblyVectorSection<ImportEntry>(SectionType.IMPORT)

class WebAssemblyFunctionSection : WebAssemblyVectorSection<TypeIndex>(SectionType.FUNCTION)

class WebAssemblyTableSection : WebAssemblyVectorSection<TableDefinition>(SectionType.TABLE)

class WebAssemblyMemorySection : WebAssemblyVectorSection<MemoryDefinition>(SectionType.MEMORY)

class WebAssemblyGlobalSection : WebAssemblyVectorSection<GlobalDefinition>(SectionType.GLOBAL)

class WebAssemblyExportSection : WebAssemblyVectorSection<ExportEntry>(SectionType.EXPORT)

class WebAssemblyStartSection(val startIndex: FuncIndex) : WebAssemblySection(SectionType.START)

class WebAssemblyElementSection : WebAssemblyVectorSection<ElementSegment>(SectionType.ELEMENT)

class WebAssemblyCodeSection : WebAssemblyVectorSection<CodeEntry>(SectionType.CODE)

class WebAssemblyDataSection : WebAssemblyVectorSection<DataSegment>(SectionType.DATA)
