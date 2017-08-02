package me.tomassetti.wasmkit

import kotlin.experimental.and

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

class WebAssemblyLoader(bytes: ByteArray, val module: WebAssemblyModule) {
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
            SectionType.GLOBAL -> readGlobalSection()
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

    private fun readGlobalSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nGlobals = bytesReader.readU32()
        println("N GLOBALS $nGlobals")
        val section = WebAssemblyGlobalSection()
        1.rangeTo(nGlobals).forEach {
            section.addGlobalDefinition(readGlobalDefinition())
        }
        return section
    }

    private fun readGlobalDefinition(): GlobalDefinition {
        return GlobalDefinition(readGlobalType(), readExpression())
    }

    private fun readGlobalType() = GlobalType(readType(), readBoolean())

    private fun readExpression(): Expression {

        expectByte(0x0B)
        throw UnsupportedOperationException()
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
