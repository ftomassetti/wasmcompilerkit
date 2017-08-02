package me.tomassetti.wasmkit

import java.io.File
import java.io.InputStream
import java.util.*

val MAGIC_NUMBER = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

typealias TableIndex = Long
typealias TypeIndex = Long
typealias FuncIndex = Long
typealias MemIndex = Long

enum class WebAssemblyVersion(val byteArray: ByteArray) {
    WASM1(byteArrayOf(0x01, 0x00, 0x00, 0x00))
}

open class WebAssemblySection

enum class ValueType(val id: Byte) {
    I32(0x7F),
    I64(0x7E),
    F32(0x7D),
    F64(0x7C);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}

enum class ImportType(val id: Byte) {
    FUNC(0x00),
    TABLE(0x01),
    MEM(0x02),
    GLOBAL(0x03);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}

data class FuncType(val paramTypes: List<ValueType>, val returnTypes: List<ValueType>)

data class ImportEntry(val module: String, val name: String, val importData: ImportData)

data class ExportEntry(val name: String, val exportData: ExportData)

data class Limits(val min: Long, val max: Long? = null)

abstract class ImportData {
    abstract fun type() : ImportType
}

data class GlobalImportData(val type: ValueType, val mutability: Boolean) : ImportData() {
    override fun type() = ImportType.GLOBAL
}

data class FunctionImportData(val typeIndex: TypeIndex) : ImportData() {
    override fun type() = ImportType.FUNC
}

data class MemoryImportData(val limits: Limits) : ImportData() {
    override fun type() = ImportType.MEM
}

data class TableImportData(val limits: Limits) : ImportData() {
    override fun type() = ImportType.TABLE
}

abstract class ExportData {
    abstract fun type() : ImportType
}

data class GlobalExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.GLOBAL
}

data class FunctionExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.FUNC
}

data class MemoryExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.MEM
}

data class TableExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.TABLE
}

class WebAssemblyTypeSection : WebAssemblySection() {
    private val funcTypes = LinkedList<FuncType>()

    fun addFuncType(funcType: FuncType) {
        funcTypes.add(funcType)
    }
}

class WebAssemblyImportSection : WebAssemblySection() {
    private val imports = LinkedList<ImportEntry>()

    fun addImport(import: ImportEntry) {
        imports.add(import)
    }
}

class WebAssemblyGlobalSection : WebAssemblySection() {
    private val globalDefinitions = LinkedList<GlobalDefinition>()

    fun addGlobalDefinition(globalDefinition: GlobalDefinition) {
        globalDefinitions.add(globalDefinition)
    }
}

data class GlobalType(val valueType: ValueType, val mutability: Boolean)

data class GlobalDefinition(val globalType: GlobalType, val expr: Instruction)

class WebAssemblyFunctionSection : WebAssemblySection() {
    private val typeIndexes = LinkedList<TypeIndex>()

    fun addTypeIndex(typeIndex: TypeIndex) {
        typeIndexes.add(typeIndex)
    }
}

class WebAssemblyExportSection : WebAssemblySection() {
    private val entries = LinkedList<ExportEntry>()

    fun addEntry(entry: ExportEntry) {
        entries.add(entry)
    }
}

class WebAssemblyDataSection : WebAssemblySection() {
    private val segments = LinkedList<DataSegment>()

    fun addSegment(segment: DataSegment) {
        segments.add(segment)
    }
}

data class DataSegment(val x: MemIndex, val e: Instruction, val data: ByteArray)

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

data class ElementSegment(val tableIndex: TableIndex, val expr: Instruction, val init: List<FuncIndex>)

class CodeBlock(val bytes: ByteArray)

data class CodeEntry(val locals: List<Pair<Long, ValueType>>, val code: CodeBlock)

class WebAssemblyCodeSection : WebAssemblySection() {
    private val entries = LinkedList<CodeEntry>()

    fun addEntry(entry: CodeEntry) {
        entries.add(entry)
    }
}

class WebAssemblyElementSection : WebAssemblySection() {
    private val segments = LinkedList<ElementSegment>()

    fun addSegment(segment: ElementSegment) {
        segments.add(segment)
    }
}

fun load(inputStream: InputStream) : WebAssemblyModule {
    return load(inputStream.readBytes())
}

fun load(bytes: ByteArray) : WebAssemblyModule {
    val module = WebAssemblyModule()
    val loader = WebAssemblyLoader(bytes, module)
    loader.readMagicNumber()
    module.version = loader.readVersion()
    while (!loader.hasFinished()) {
        loader.readSection()
        //throw RuntimeException("unread content: remaining bytes ${loader.remainingBytes()}")
    }
    return module
}

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

fun main(args: Array<String>) {
    //0,97,115,109,1,0,0,0,1,8,2,96,1,127,0,96,0,0,2,8,1,2,106,115,1,95,0,0,3,2,1,1,8,1,1,10,9,1,7,0,65,185,10,16,0,11
    val module = WebAssemblyModule()
    File("example.wasm").writeBytes(module.generateBytes())
}
