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

abstract class WebAssemblySection(val type : SectionType) : Sized

abstract class WebAssemblyVectorSection<E:Any>(type : SectionType) : WebAssemblySection(type) {
    private val _elements = LinkedList<E>()

    val elements : List<E>
        get() = _elements.toList()

    fun addElement(element: E) {
        _elements.add(element)
    }

    fun nElements() = _elements.size

    override fun sizeInBytes(): Long {
        // 1 for the type of the section
        // 5 for the dimension of the section
        val elementsSize = elements.foldRight(0L, { el, acc -> acc + elementSize(el) })
        return 6 + elementsSize
    }

    private fun elementSize(element: Any): Long {
        return when (element) {
            is Sized -> element.sizeInBytes()
            else -> TODO(element.toString())
        }
    }
}

class WebAssemblyCustomSection(val name: String, val data: ByteArray) : WebAssemblySection(SectionType.CUSTOM) {
    override fun sizeInBytes(): Long {
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
    override fun sizeInBytes(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class WebAssemblyElementSection : WebAssemblyVectorSection<ElementSegment>(SectionType.ELEMENT)

class WebAssemblyCodeSection : WebAssemblyVectorSection<CodeEntry>(SectionType.CODE)

class WebAssemblyDataSection : WebAssemblyVectorSection<DataSegment>(SectionType.DATA)
