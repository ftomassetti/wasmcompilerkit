import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

val MAGIC_NUMBER = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

enum class WebAssemblyVersion(val byteArray: ByteArray) {
    WASM1(byteArrayOf(0x01, 0x00, 0x00, 0x00))
}

open class WebAssemblySection

class Type

data class FuncType(val paramTypes: List<Type>, val returnTypes: List<Type>)

class WebAssemblyTypeSection : WebAssemblySection() {
    private val funcTypes = LinkedList<FuncType>()

    fun addFuncType(funcType: FuncType) {
        funcTypes.add(funcType)
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
        fun fromId(id: Byte) = SectionType.values().first { it.id == id }
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
    fun  readS32(): Long {
        val byte = readNextByte()
        return byte.and(0x7F).toLong() + if (byte.and(0x80.toByte()) != 0.toByte()) 128 * readU32() else 0
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
            else -> throw RuntimeException("FOUND ${sectionType}")
        }
        module.addSection(section)
    }

    private fun readTypeSection() : WebAssemblySection {
        val length = bytesReader.readU32()
        val typeSection = WebAssemblyTypeSection()
        1.rangeTo(length).forEach {
            println("READING FUNC TYPE $it ${bytesReader.currentIndex()}")
            typeSection.addFuncType(readFuncType())
        }
        return typeSection
    }

    private fun readFuncType(): FuncType {
        expectByte(0x60)
        throw UnsupportedOperationException()
    }
}

fun main(args: Array<String>) {
    //0,97,115,109,1,0,0,0,1,8,2,96,1,127,0,96,0,0,2,8,1,2,106,115,1,95,0,0,3,2,1,1,8,1,1,10,9,1,7,0,65,185,10,16,0,11
    val module = WebAssemblyModule()
    File("example.wasm").writeBytes(module.generateBytes())
}
