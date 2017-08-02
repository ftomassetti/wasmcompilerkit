import java.io.File
import java.io.InputStream

val MAGIC_NUMBER = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

enum class WebAssemblyVersion(val number: Int) {
    WASM1(1)
}

class WebAssemblyModule(val version: WebAssemblyVersion = WebAssemblyVersion.WASM1) {

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
        byteArray[index + 0] = version.number.toByte()
        byteArray[index + 1] = 0x00
        byteArray[index + 2] = 0x00
        byteArray[index + 3] = 0x00
    }

}

fun load(inputStream: InputStream) : WebAssemblyModule {
    return load(inputStream.readBytes())
}

fun load(bytes: ByteArray) : WebAssemblyModule {
    val module = WebAssemblyModule()
    val loader = WebAssemblyLoader(bytes, module)
    loader.readMagicNumber()
    if (!loader.hasFinished()) {
        throw RuntimeException("unread content")
    }
    return module
}

private class WebAssemblyLoader(val bytes: ByteArray, val module: WebAssemblyModule) {
    private var currentIndex = 0

    fun readMagicNumber() {
        MAGIC_NUMBER.forEach { expectByte(it) }
    }

    fun expectByte(expectedByte: Byte) {
        if (bytes[currentIndex] != expectedByte) {
            throw RuntimeException("Invalid byte found")
        }
        currentIndex++
    }

    fun hasFinished() = bytes.size == currentIndex
}

fun main(args: Array<String>) {
    //0,97,115,109,1,0,0,0,1,8,2,96,1,127,0,96,0,0,2,8,1,2,106,115,1,95,0,0,3,2,1,1,8,1,1,10,9,1,7,0,65,185,10,16,0,11
    val module = WebAssemblyModule()
    File("example.wasm").writeBytes(module.generateBytes())
}
