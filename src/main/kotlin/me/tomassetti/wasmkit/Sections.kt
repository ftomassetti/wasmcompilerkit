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

open class WebAssemblySection(val type : SectionType)

class WebAssemblyCustomSection(val name: String, val data: ByteArray) : WebAssemblySection(SectionType.CUSTOM)

class WebAssemblyTypeSection : WebAssemblySection(SectionType.TYPE) {
    private val funcTypes = LinkedList<FuncType>()

    fun addFuncType(funcType: FuncType) {
        funcTypes.add(funcType)
    }
}

class WebAssemblyImportSection : WebAssemblySection(SectionType.IMPORT) {
    private val imports = LinkedList<ImportEntry>()

    fun addImport(import: ImportEntry) {
        imports.add(import)
    }
}

class WebAssemblyFunctionSection : WebAssemblySection(SectionType.FUNCTION) {
    private val typeIndexes = LinkedList<TypeIndex>()

    fun addTypeIndex(typeIndex: TypeIndex) {
        typeIndexes.add(typeIndex)
    }
}

class WebAssemblyTableSection : WebAssemblySection(SectionType.TABLE) {
    private val tables = LinkedList<TableDefinition>()

    fun addTable(table: TableDefinition) {
        tables.add(table)
    }
}

class WebAssemblyMemorySection : WebAssemblySection(SectionType.MEMORY) {
    private val memories = LinkedList<MemoryDefinition>()

    fun addMemory(memory: MemoryDefinition) {
        memories.add(memory)
    }
}

class WebAssemblyGlobalSection : WebAssemblySection(SectionType.GLOBAL) {
    private val globalDefinitions = LinkedList<GlobalDefinition>()

    fun addGlobalDefinition(globalDefinition: GlobalDefinition) {
        globalDefinitions.add(globalDefinition)
    }
}

class WebAssemblyExportSection : WebAssemblySection(SectionType.EXPORT) {
    private val entries = LinkedList<ExportEntry>()

    fun addEntry(entry: ExportEntry) {
        entries.add(entry)
    }
}

class WebAssemblyStartSection(val startIndex: FuncIndex) : WebAssemblySection(SectionType.START)

class WebAssemblyElementSection : WebAssemblySection(SectionType.ELEMENT) {
    private val segments = LinkedList<ElementSegment>()

    fun addSegment(segment: ElementSegment) {
        segments.add(segment)
    }
}

class WebAssemblyCodeSection : WebAssemblySection(SectionType.CODE) {
    private val entries = LinkedList<CodeEntry>()

    fun addEntry(entry: CodeEntry) {
        entries.add(entry)
    }
}

class WebAssemblyDataSection : WebAssemblySection(SectionType.DATA) {
    private val segments = LinkedList<DataSegment>()

    fun addSegment(segment: DataSegment) {
        segments.add(segment)
    }
}
