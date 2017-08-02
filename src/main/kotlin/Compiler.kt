import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

val MAGIC_NUMBER = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

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

typealias TypeIndex = Long

class WebAssemblyFunctionSection : WebAssemblySection() {
    private val typeIndexes = LinkedList<TypeIndex>()

    fun addTypeIndex(typeIndex: TypeIndex) {
        typeIndexes.add(typeIndex)
    }
}

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

class BytesReader(val bytes: ByteArray) {
    private var currentIndex = 0

    fun readNextByte() = bytes[currentIndex++]

    fun hasFinished() = remainingBytes() == 0

    fun remainingBytes() = bytes.size - currentIndex

    fun readU32() : Long {
        val byte = readNextByte()
        return byte.and(0x7F).toLong() + if (byte.and(0x80.toByte()) != 0.toByte()) 128 * readU32() else 0
    }

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

    private fun readBytesSequence() : ByteArray {
        val byte = readNextByte()
        if (byte.and(0x80.toByte()) != 0.toByte()) {
            return byteArrayOf(byte) + readBytesSequence()
        } else {
            return byteArrayOf(byte)
        }
    }

}

private class WebAssemblyLoader(bytes: ByteArray, val module: WebAssemblyModule) {
    private val bytesReader = BytesReader(bytes)

    fun readMagicNumber() {
        MAGIC_NUMBER.forEach { expectByte(it) }
    }

    fun readVersion() : WebAssemblyVersion {
        WebAssemblyVersion.WASM1.byteArray.forEach { expectByte(it) }
        return WebAssemblyVersion.WASM1
    }

    fun expectByte(expectedByte: Byte) {
        val readByte = readNextByte()
        if (readByte != expectedByte) {
            throw RuntimeException("Invalid byte found. Expected $expectedByte found $readByte")
        }
    }

    fun readNextByte() = bytesReader.readNextByte()

    fun hasFinished() = bytesReader.hasFinished()

    fun readSection() {
        val sectionType = SectionType.fromId(readNextByte())
        val section = when (sectionType) {
            SectionType.TYPE -> readTypeSection()
            SectionType.IMPORT -> readImportSection()
            SectionType.FUNCTION -> readFunctionSection()
            else -> throw RuntimeException("FOUND $sectionType")
        }
        module.addSection(section)
    }

    private fun readTypeSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        println("TYPE SECTION: payloadLen=$payloadLen nFunctions=$nFunctions")
        val section = WebAssemblyTypeSection()
        1.rangeTo(nFunctions).forEach {
            section.addFuncType(readFuncType())
        }
        return section
    }

    private fun readImportSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nImports = bytesReader.readU32()
        println("N IMPORTS ${nImports}")
        val section = WebAssemblyImportSection()
        1.rangeTo(nImports).forEach {
            section.addImport(readImportEntry())
        }
        return section
    }

    private fun readFunctionSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        println("N FUNCTIONS $nFunctions")
        val section = WebAssemblyFunctionSection()
        1.rangeTo(nFunctions).forEach {
            section.addTypeIndex(bytesReader.readU32())
        }
        return section
    }

    private fun readImportEntry(): ImportEntry {
        val module = readName()
        val entry = readName()
        val importType = ImportType.fromId(readNextByte())
        val importData : ImportData = when (importType) {
            ImportType.GLOBAL -> readGlobalImportData()
            ImportType.FUNC -> readFunctionImportData()
            ImportType.MEM -> readMemoryImportData()
            ImportType.TABLE -> readTableImportData()
            else -> throw UnsupportedOperationException("I do not know how to read $importType")
        }
        return ImportEntry(module, entry, importData)
    }

    private fun readName(): String {
        val length = bytesReader.readU32()
        val bytes = 1.rangeTo(length).map { readNextByte() }.toByteArray()
        val name = String(bytes)
        return name
    }

    private fun readFuncType(): FuncType {
        expectByte(0x60)
        val nArgs = bytesReader.readU32()
        val args = 1.rangeTo(nArgs).map { readType() }
        val nReturns = bytesReader.readU32()
        val returns = 1.rangeTo(nReturns).map { readType() }

        return FuncType(args, returns)
    }

    private fun readGlobalImportData() : GlobalImportData {
        return GlobalImportData(readType(), readBoolean())
    }

    private fun readFunctionImportData() : FunctionImportData {
        return FunctionImportData(bytesReader.readU32())
    }

    private fun readMemoryImportData() : MemoryImportData {
        return MemoryImportData(readLimits())
    }

    private fun readTableImportData() : TableImportData {
        expectByte(0x70)
        return TableImportData(readLimits())
    }

    private fun readBoolean(): Boolean {
        val b = readNextByte()
        when (b) {
            0x00.toByte() -> return false
            0x01.toByte() -> return true
            else -> throw RuntimeException()
        }
    }

    private fun readLimits(): Limits {
        val b = readNextByte()
        when (b) {
            0x00.toByte() -> return Limits(bytesReader.readU32())
            0x01.toByte() -> return Limits(bytesReader.readU32(), bytesReader.readU32())
            else -> throw RuntimeException()
        }
    }

    private fun readType() = ValueType.fromId(readNextByte())
}

fun main(args: Array<String>) {
    //0,97,115,109,1,0,0,0,1,8,2,96,1,127,0,96,0,0,2,8,1,2,106,115,1,95,0,0,3,2,1,1,8,1,1,10,9,1,7,0,65,185,10,16,0,11
    val module = WebAssemblyModule()
    File("example.wasm").writeBytes(module.generateBytes())
}
