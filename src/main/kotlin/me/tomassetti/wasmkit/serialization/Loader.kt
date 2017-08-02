package me.tomassetti.wasmkit.serialization

import me.tomassetti.wasmkit.*

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
            SectionType.EXPORT -> readExportSection()
            SectionType.FUNCTION -> readFunctionSection()
            SectionType.GLOBAL -> readGlobalSection()
            SectionType.ELEMENT -> readElementSection()
            SectionType.CODE -> readCodeSection()
            SectionType.DATA -> readDataSection()
            SectionType.TABLE -> readTableSection()
            SectionType.MEMORY -> readMemorySection()
            SectionType.CUSTOM -> TODO()
            SectionType.START -> TODO()
        }
        module.addSection(section)
    }

    private fun readMemorySection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyMemorySection()
        1.rangeTo(nElements).forEach {
            section.addMemory(MemoryDefinition(readLimits()))
        }
        return section
    }

    private fun readTableSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyTableSection()
        1.rangeTo(nElements).forEach {
            section.addTable(readTableDefinition())
        }
        return section
    }

    private fun readTableDefinition(): TableDefinition {
        expectByte(0x70)
        return TableDefinition(readLimits())
    }

    private fun readDataSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyDataSection()
        1.rangeTo(nElements).forEach {
            val x = bytesReader.readU32()
            val e = readExpression()
            val nBytes = bytesReader.readU32()
            val b = bytesReader.readBytes(nBytes)
            section.addSegment(DataSegment(x, e, b))
        }
        return section
    }

    private fun readCodeSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyCodeSection()
        1.rangeTo(nElements).forEach {
            val codeSize = bytesReader.readU32()
            val startPos = bytesReader.currentIndex()
            val nLocals = bytesReader.readU32()
            val locals = 1.rangeTo(nLocals).map { Pair(bytesReader.readU32(), readType()) }
            val currentPos = bytesReader.currentIndex()
            val bytesToRead = codeSize - (currentPos - startPos)
            val codeBytes = bytesReader.readBytes(bytesToRead)
            section.addEntry(CodeEntry(locals, CodeBlock(codeBytes)))
        }
        return section
    }

    private fun readTypeSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        val section = WebAssemblyTypeSection()
        1.rangeTo(nFunctions).forEach {
            section.addFuncType(readFuncType())
        }
        return section
    }

    private fun readImportSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nImports = bytesReader.readU32()
        val section = WebAssemblyImportSection()
        1.rangeTo(nImports).forEach {
            section.addImport(readImportEntry())
        }
        return section
    }

    private fun readExportSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyExportSection()
        1.rangeTo(nElements).forEach {
            section.addEntry(readExportEntry())
        }
        return section
    }

    private fun readFunctionSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        val section = WebAssemblyFunctionSection()
        1.rangeTo(nFunctions).forEach {
            section.addTypeIndex(bytesReader.readU32())
        }
        return section
    }

    private fun readGlobalSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nGlobals = bytesReader.readU32()
        val section = WebAssemblyGlobalSection()
        1.rangeTo(nGlobals).forEach {
            section.addGlobalDefinition(readGlobalDefinition())
        }
        return section
    }

    private fun readElementSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyElementSection()
        1.rangeTo(nElements).forEach {
            section.addSegment(readElementSegment())
        }
        return section
    }

    private fun readElementSegment(): ElementSegment {
        val x = bytesReader.readU32()
        val e = readExpression()
        val nInit = bytesReader.readU32()
        val init = 1.rangeTo(nInit).map { bytesReader.readU32() }
        return ElementSegment(x, e, init)
    }

    private fun readGlobalDefinition(): GlobalDefinition {
        return GlobalDefinition(readGlobalType(), readExpression())
    }

    private fun readGlobalType() = GlobalType(readType(), readBoolean())

    private fun readExpression(): Instruction {
        val instructionType = InstructionType.fromOpcode(bytesReader.readNextByte())
        val instruction = when (instructionType.family) {
            InstructionFamily.VAR -> VarInstruction(instructionType, bytesReader.readU32())
            InstructionFamily.NUMERIC_CONST -> {
                when (instructionType) {
                    InstructionType.I32CONST -> I32ConstInstruction(instructionType, bytesReader.readU32())
                    else -> throw UnsupportedOperationException("Instruction $instructionType")
                }
            }
            else -> throw UnsupportedOperationException("Instruction $instructionType")
        }
        expectByte(0x0B)
        return instruction
    }

    private fun readImportData() : ImportData {
        val importType = ImportType.fromId(readNextByte())
        val importData : ImportData = when (importType) {
            ImportType.GLOBAL -> readGlobalImportData()
            ImportType.FUNC -> readFunctionImportData()
            ImportType.MEM -> readMemoryImportData()
            ImportType.TABLE -> readTableImportData()
            else -> throw UnsupportedOperationException("I do not know how to read $importType")
        }
        return importData
    }

    private fun readExportData() : ExportData {
        val importType = ImportType.fromId(readNextByte())
        val data : ExportData = when (importType) {
            ImportType.GLOBAL -> GlobalExportData(bytesReader.readU32())
            ImportType.FUNC -> TableExportData(bytesReader.readU32())
            ImportType.MEM -> MemoryExportData(bytesReader.readU32())
            ImportType.TABLE -> TableExportData(bytesReader.readU32())
            else -> throw UnsupportedOperationException("I do not know how to read $importType")
        }
        return data
    }

    private fun readExportEntry(): ExportEntry {
        val entry = readName()
        val exportData = readExportData()
        return ExportEntry(entry, exportData)
    }

    private fun readImportEntry(): ImportEntry {
        val module = readName()
        val entry = readName()
        val importData = readImportData()
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
