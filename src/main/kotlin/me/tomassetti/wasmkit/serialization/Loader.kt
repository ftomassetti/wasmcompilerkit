package me.tomassetti.wasmkit.serialization

import me.tomassetti.wasmkit.*
import java.util.*

sealed class BlockType
object emptyBlockType : BlockType() {
    override fun toString() = "emptyBlockType"
}
data class ValuedBlockType(val valueType: ValueType) : BlockType()

val ELSE_BYTE = 0x05.toByte()
val END_BYTE = 0x0B.toByte()

private fun readBlockType(bytesReader: BytesReader) : BlockType {
    val byte = bytesReader.readNextByte()
    if (byte == 0x40.toByte()) {
        return emptyBlockType
    } else {
        return ValuedBlockType(ValueType.fromId(byte))
    }
}

fun readExpression(bytesReader: BytesReader, delimiterExpected : Boolean = true): Instruction {
    val instructionType = InstructionType.fromOpcode(bytesReader.readNextByte())
    val instruction = when (instructionType.family) {
        InstructionFamily.VAR -> VarInstruction(instructionType, bytesReader.readU32())
        InstructionFamily.NUMERIC_CONST -> {
            when (instructionType) {
                InstructionType.I32CONST -> I32ConstInstruction(bytesReader.readS32())
                InstructionType.I64CONST -> I32ConstInstruction(bytesReader.readU32())
                InstructionType.F32CONST -> F32ConstInstruction(bytesReader.readFloat())
                InstructionType.F64CONST -> F64ConstInstruction(bytesReader.readDouble())
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.BLOCKS -> {
            when (instructionType) {
                InstructionType.BLOCK -> {
                    val blockType = readBlockType(bytesReader)
                    val instructions = LinkedList<Instruction>()
                    while (bytesReader.peekNextByte() != END_BYTE) {
                        instructions.add(readExpression(bytesReader, delimiterExpected = false))
                    }
                    bytesReader.expectByte(END_BYTE)
                    BlockInstruction(blockType, instructions)
                }
                InstructionType.LOOP -> {
                    val blockType = readBlockType(bytesReader)
                    val instructions = LinkedList<Instruction>()
                    while (bytesReader.peekNextByte() != END_BYTE) {
                        instructions.add(readExpression(bytesReader, delimiterExpected = false))
                    }
                    bytesReader.expectByte(END_BYTE)
                    LoopInstruction(blockType, instructions)
                }
                InstructionType.IF -> {
                    val blockType = readBlockType(bytesReader)
                    val thenInstructions = LinkedList<Instruction>()
                    var elseInstructions: MutableList<Instruction>? = null
                    while (bytesReader.peekNextByte() != ELSE_BYTE && bytesReader.peekNextByte() != END_BYTE) {
                        thenInstructions.add(readExpression(bytesReader, delimiterExpected = false))
                    }
                    if (bytesReader.peekNextByte() == ELSE_BYTE) {
                        bytesReader.expectByte(ELSE_BYTE)
                        elseInstructions = LinkedList<Instruction>()
                        while (bytesReader.peekNextByte() != END_BYTE) {
                            elseInstructions.add(readExpression(bytesReader, delimiterExpected = false))
                        }
                    }
                    bytesReader.expectByte(END_BYTE)
                    IfInstruction(blockType, thenInstructions, elseInstructions)
                }
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.COMPARISON_OP -> {
            when (instructionType) {
                InstructionType.I32GTS, InstructionType.I32GTU,
                InstructionType.I32LTS, InstructionType.I32LTU,
                InstructionType.I32GES, InstructionType.I32GEU,
                InstructionType.I32LES, InstructionType.I32LEU,
                InstructionType.I64GTS, InstructionType.I64GTU,
                InstructionType.I64LTS, InstructionType.I64LTU,
                InstructionType.I64GES, InstructionType.I64GEU,
                InstructionType.I64LES, InstructionType.I64LEU,
                InstructionType.F32GT, InstructionType.F32GE,
                InstructionType.F32LT, InstructionType.F32LE,
                InstructionType.F64GT, InstructionType.F64GE,
                InstructionType.F64LT, InstructionType.F64LE,
                InstructionType.I32NE, InstructionType.I64NE,
                InstructionType.F32NE, InstructionType.F64NE,
                InstructionType.I32EQ, InstructionType.I64EQ,
                InstructionType.F32EQ, InstructionType.F64EQ-> {
                    BinaryComparison(instructionType)
                }
                InstructionType.I32EQZ -> {
                    I32EqzInstruction
                }
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.NUMERIC_OP -> {
            when (instructionType) {

                InstructionType.I32ADD -> I32AddInstruction
                InstructionType.I32SUB -> I32SubInstruction
                InstructionType.I32MUL -> I32MulInstruction
                InstructionType.I32DIVS -> I32DivSInstruction
                InstructionType.I32DIVU -> I32DivUInstruction
                InstructionType.I32REMS -> I32RemSInstruction
                InstructionType.I32REMU -> I32RemUInstruction
                InstructionType.I32AND -> I32AndInstruction
                InstructionType.I32OR -> I32OrInstruction
                InstructionType.I32XOR -> I32XorInstruction

                InstructionType.I64ADD -> I64AddInstruction
                InstructionType.I64SUB -> I64SubInstruction
                InstructionType.I64MUL -> I64MulInstruction
                InstructionType.I64DIVS -> I64DivSInstruction
                InstructionType.I64DIVU -> I64DivUInstruction
                InstructionType.I64REMS -> I64RemSInstruction
                InstructionType.I64REMU -> I64RemUInstruction
                InstructionType.I64AND -> I64AndInstruction
                InstructionType.I64OR -> I64OrInstruction
                InstructionType.I64XOR -> I64XorInstruction

                InstructionType.F32ADD -> F32AddInstruction
                InstructionType.F32SUB -> F32SubInstruction
                InstructionType.F32MUL -> F32MulInstruction
                InstructionType.F32DIV -> F32DivInstruction

                InstructionType.F64ADD -> F64AddInstruction
                InstructionType.F64SUB -> F64SubInstruction
                InstructionType.F64MUL -> F64MulInstruction
                InstructionType.F64DIV -> F64DivInstruction

                InstructionType.F32SQRT -> F32SqrtInstruction
                InstructionType.F64SQRT -> F64SqrtInstruction

                InstructionType.F32CEIL -> F32CeilInstruction
                InstructionType.F64CEIL -> F64CeilInstruction
                InstructionType.F32FLOOR -> F32FloorInstruction
                InstructionType.F64FLOOR -> F64FloorInstruction
                
                InstructionType.I32SHL -> I32ShlInstruction
                InstructionType.I32SHRU -> I32ShruInstruction
                InstructionType.I32SHRS -> I32ShrsInstruction
                InstructionType.I32ROTL -> I32RotlInstruction
                InstructionType.I32ROTR -> I32RotrInstruction

                InstructionType.I64SHL -> I64ShlInstruction
                InstructionType.I64SHRU -> I64ShruInstruction
                InstructionType.I64SHRS -> I64ShrsInstruction
                InstructionType.I64ROTL -> I64RotlInstruction
                InstructionType.I64ROTR -> I64RotrInstruction

                InstructionType.F32NEG -> F32NegInstruction
                InstructionType.F64NEG -> F64NegInstruction

                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.CONVERSION_OP -> {
            when (instructionType) {

                InstructionType.I32TRUNCSF32 -> I32TruncSF32Instruction
                InstructionType.I32TRUNCSF64 -> I32TruncSF64Instruction
                InstructionType.I32TRUNCUF32 -> I32TruncUF32Instruction
                InstructionType.I32TRUNCUF64 -> I32TruncUF64Instruction

                InstructionType.I32WRAPI64 -> I32WrapI64Instruction

                InstructionType.F64PROMOTEF32 -> F64PromoteF32Instruction

                InstructionType.F32CONVERTUI32 -> F32ConvertUI32Instruction
                InstructionType.F32CONVERTUI64 -> F32ConvertUI64Instruction
                InstructionType.F32CONVERTSI32 -> F32ConvertSI32Instruction
                InstructionType.F32CONVERTSI64 -> F32ConvertSI64Instruction

                InstructionType.F64CONVERTUI32 -> F64ConvertUI32Instruction
                InstructionType.F64CONVERTUI64 -> F64ConvertUI64Instruction
                InstructionType.F64CONVERTSI32 -> F64ConvertSI32Instruction
                InstructionType.F64CONVERTSI64 -> F64ConvertSI64Instruction
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.CONTROL -> {
            when (instructionType) {
                InstructionType.RETURN -> returnInstruction
                InstructionType.JUMP -> {
                    JumpInstruction(bytesReader.readU32())
                }
                InstructionType.CONDJUMP -> {
                    ConditionalJumpInstruction(bytesReader.readU32())
                }
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        InstructionFamily.MEMORY -> {
            when (instructionType) {
                InstructionType.GROWMEM -> {
                    bytesReader.expectByte(0)
                    GrowMem
                }
                InstructionType.CURRMEM -> {
                    bytesReader.expectByte(0)
                    CurrMem
                }
                else -> {
                    val align = bytesReader.readU32()
                    val offset = bytesReader.readU32()
                    MemoryInstruction(instructionType, MemoryPosition(align, offset))
                }
            }
        }
        InstructionFamily.CALL -> {
            when (instructionType) {
                InstructionType.CALL -> CallInstruction(bytesReader.readU32())
                InstructionType.INDCALL -> IndirectCallInstruction(bytesReader.readU32())
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
        else -> {
            when (instructionType) {
                InstructionType.NOP -> nop
                InstructionType.UNREACHABLE -> unreachable
                InstructionType.DROP -> drop
                InstructionType.SELECT -> select
                else -> throw UnsupportedOperationException("Instruction $instructionType")
            }
        }
    }
    if (delimiterExpected) {
        bytesReader.expectByte(END_BYTE)
    }
    return instruction
}

fun CodeBlock.interpret() : List<Instruction> {
    val bytesReader = BytesReader(this.bytes)
    val instructions = LinkedList<Instruction>()
    while (bytesReader.peekNextByte() != END_BYTE) {
        val instruction = readExpression(bytesReader, delimiterExpected = false)
        instructions.add(instruction)
    }
    bytesReader.expectByte(END_BYTE)
    if (!bytesReader.hasFinished()) {
        throw IllegalStateException("Expected to finish byte. Remaining: ${bytesReader.remainingBytes()}")
    }
    return instructions
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
            section.addElement(MemoryDefinition(readLimits()))
        }
        return section
    }

    private fun readTableSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyTableSection()
        1.rangeTo(nElements).forEach {
            section.addElement(readTableDefinition())
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
            section.addElement(DataSegment(x, e, b))
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
            section.addElement(CodeEntry(locals, CodeBlock(codeBytes)))
        }
        return section
    }

    private fun readTypeSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        val section = WebAssemblyTypeSection()
        1.rangeTo(nFunctions).forEach {
            section.addElement(readFuncType())
        }
        return section
    }

    private fun readImportSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nImports = bytesReader.readU32()
        val section = WebAssemblyImportSection()
        1.rangeTo(nImports).forEach {
            section.addElement(readImportEntry())
        }
        return section
    }

    private fun readExportSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyExportSection()
        1.rangeTo(nElements).forEach {
            section.addElement(readExportEntry())
        }
        return section
    }

    private fun readFunctionSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nFunctions = bytesReader.readU32()
        val section = WebAssemblyFunctionSection()
        1.rangeTo(nFunctions).forEach {
            section.addElement(bytesReader.readU32())
        }
        return section
    }

    private fun readGlobalSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nGlobals = bytesReader.readU32()
        val section = WebAssemblyGlobalSection()
        1.rangeTo(nGlobals).forEach {
            section.addElement(readGlobalDefinition())
        }
        return section
    }

    private fun readElementSection() : WebAssemblySection {
        val payloadLen = bytesReader.readU32()
        val nElements = bytesReader.readU32()
        val section = WebAssemblyElementSection()
        1.rangeTo(nElements).forEach {
            section.addElement(readElementSegment())
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

    private fun readExpression() = readExpression(bytesReader)

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
            ImportType.FUNC -> FunctionExportData(bytesReader.readU32())
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
