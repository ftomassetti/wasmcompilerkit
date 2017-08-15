package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.BlockType
import me.tomassetti.wasmkit.serialization.sizeInBytesOfU32

enum class InstructionFamily {
    UNSPECIFIED,
    VAR,
    NUMERIC_CONST,
    BLOCKS,
    NUMERIC_OP,
    CONTROL,
    MEMORY,
    CALL,
    CONVERSION_OP,
    COMPARISON_OP
}

enum class InstructionType(val opcode: Byte, val family: InstructionFamily = InstructionFamily.UNSPECIFIED) {
    UNREACHABLE(0x00),
    NOP(0x01),
    BLOCK(0x02, InstructionFamily.BLOCKS),
    LOOP(0x03, InstructionFamily.BLOCKS),
    IF(0x04, InstructionFamily.BLOCKS),
    JUMP(0x0C, InstructionFamily.CONTROL),
    CONDJUMP(0x0D, InstructionFamily.CONTROL),
    TABLEJUMP(0x0E),
    RETURN(0x0F, InstructionFamily.CONTROL),
    CALL(0x10, InstructionFamily.CALL),
    INDCALL(0x11, InstructionFamily.CALL),
    DROP(0x1A),
    SELECT(0x1B),

    GET_LOCAL(0x20, InstructionFamily.VAR),
    SET_LOCAL(0x21, InstructionFamily.VAR),
    TEE_LOCAL(0x22, InstructionFamily.VAR),
    GET_GLOBAL(0x23, InstructionFamily.VAR),
    SET_GLOBAL(0x24, InstructionFamily.VAR),

    LOADI32(0x28, InstructionFamily.MEMORY),
    LOADI64(0x29, InstructionFamily.MEMORY),
    LOADF32(0x2A, InstructionFamily.MEMORY),
    LOADF64(0x2B, InstructionFamily.MEMORY),
    LOADI32_8S(0x2C, InstructionFamily.MEMORY),
    LOADI32_8U(0x2D, InstructionFamily.MEMORY),
    LOADI32_16S(0x2E, InstructionFamily.MEMORY),
    LOADI32_16U(0x2F, InstructionFamily.MEMORY),
    LOADI64_8S(0x30, InstructionFamily.MEMORY),
    LOADI64_8U(0x31, InstructionFamily.MEMORY),
    LOADI64_16S(0x32, InstructionFamily.MEMORY),
    LOADI64_16U(0x33, InstructionFamily.MEMORY),
    LOADI64_32S(0x34, InstructionFamily.MEMORY),
    LOADI64_32U(0x35, InstructionFamily.MEMORY),
    STOREI32(0x36, InstructionFamily.MEMORY),
    STOREI64(0x37, InstructionFamily.MEMORY),
    STOREF32(0x38, InstructionFamily.MEMORY),
    STOREF64(0x39, InstructionFamily.MEMORY),
    STOREI32_8(0x3A, InstructionFamily.MEMORY),
    STOREI32_16(0x3B, InstructionFamily.MEMORY),
    STOREI64_8(0x3C, InstructionFamily.MEMORY),
    STOREI64_16(0x3D, InstructionFamily.MEMORY),
    STOREI64_32(0x3E, InstructionFamily.MEMORY),
    CURRMEM(0x3F, InstructionFamily.MEMORY),
    GROWMEM(0x40, InstructionFamily.MEMORY),

    I32CONST(0x41, InstructionFamily.NUMERIC_CONST),
    I64CONST(0x42, InstructionFamily.NUMERIC_CONST),
    F32CONST(0x43, InstructionFamily.NUMERIC_CONST),
    F64CONST(0x44, InstructionFamily.NUMERIC_CONST),

    I32EQZ(0x45, InstructionFamily.COMPARISON_OP),
    I32EQ(0x46, InstructionFamily.COMPARISON_OP),
    I32NE(0x47, InstructionFamily.COMPARISON_OP),
    I32LTS(0x48, InstructionFamily.COMPARISON_OP),
    I32LTU(0x49, InstructionFamily.COMPARISON_OP),
    I32GTS(0x4A, InstructionFamily.COMPARISON_OP),
    I32GTU(0x4B, InstructionFamily.COMPARISON_OP),
    I32LES(0x4C, InstructionFamily.COMPARISON_OP),
    I32LEU(0x4D, InstructionFamily.COMPARISON_OP),
    I32GES(0x4E, InstructionFamily.COMPARISON_OP),
    I32GEU(0x4F, InstructionFamily.COMPARISON_OP),
    I64EQZ(0x50, InstructionFamily.COMPARISON_OP),
    I64EQ(0x51, InstructionFamily.COMPARISON_OP),
    I64NE(0x52, InstructionFamily.COMPARISON_OP),
    I64LTS(0x53, InstructionFamily.COMPARISON_OP),
    I64LTU(0x54, InstructionFamily.COMPARISON_OP),
    I64GTS(0x55, InstructionFamily.COMPARISON_OP),
    I64GTU(0x56, InstructionFamily.COMPARISON_OP),
    I64LES(0x57, InstructionFamily.COMPARISON_OP),
    I64LEU(0x58, InstructionFamily.COMPARISON_OP),
    I64GES(0x59, InstructionFamily.COMPARISON_OP),
    I64GEU(0x5A, InstructionFamily.COMPARISON_OP),

    F32EQ(0x5B, InstructionFamily.COMPARISON_OP),
    F32NE(0x5C, InstructionFamily.COMPARISON_OP),
    F32LT(0x5D, InstructionFamily.COMPARISON_OP),
    F32GT(0x5E, InstructionFamily.COMPARISON_OP),
    F32LE(0x5F, InstructionFamily.COMPARISON_OP),
    F32GE(0x60, InstructionFamily.COMPARISON_OP),

    F64EQ(0x61, InstructionFamily.COMPARISON_OP),
    F64NE(0x62, InstructionFamily.COMPARISON_OP),
    F64LT(0x63, InstructionFamily.COMPARISON_OP),
    F64GT(0x64, InstructionFamily.COMPARISON_OP),
    F64LE(0x65, InstructionFamily.COMPARISON_OP),
    F64GE(0x66, InstructionFamily.COMPARISON_OP),

    I32CLZ(0x67),
    I32CTX(0x68),
    I32POPCNT(0x69),
    I32ADD(0x6A, InstructionFamily.NUMERIC_OP),
    I32SUB(0x6B, InstructionFamily.NUMERIC_OP),
    I32MUL(0x6C, InstructionFamily.NUMERIC_OP),
    I32DIVS(0x6D, InstructionFamily.NUMERIC_OP),
    I32DIVU(0x6E, InstructionFamily.NUMERIC_OP),
    I32REMS(0x6F, InstructionFamily.NUMERIC_OP),
    I32REMU(0x70, InstructionFamily.NUMERIC_OP),
    I32AND(0x71, InstructionFamily.NUMERIC_OP),
    I32OR(0x72, InstructionFamily.NUMERIC_OP),
    I32XOR(0x73, InstructionFamily.NUMERIC_OP),
    I32SHL(0x74, InstructionFamily.NUMERIC_OP),
    I32SHRS(0x75, InstructionFamily.NUMERIC_OP),
    I32SHRU(0x76, InstructionFamily.NUMERIC_OP),
    I32ROTL(0x77, InstructionFamily.NUMERIC_OP),
    I32ROTR(0x78, InstructionFamily.NUMERIC_OP),

    I64CLZ(0x79),
    I64CTX(0x7A),
    I64POPCNT(0x7B),
    I64ADD(0x7C, InstructionFamily.NUMERIC_OP),
    I64SUB(0x7D, InstructionFamily.NUMERIC_OP),
    I64MUL(0x7E, InstructionFamily.NUMERIC_OP),
    I64DIVS(0x7F, InstructionFamily.NUMERIC_OP),
    I64DIVU(0x80.toByte(), InstructionFamily.NUMERIC_OP),
    I64REMS(0x81.toByte(), InstructionFamily.NUMERIC_OP),
    I64REMU(0x82.toByte(), InstructionFamily.NUMERIC_OP),
    I64AND(0x83.toByte(), InstructionFamily.NUMERIC_OP),
    I64OR(0x84.toByte(), InstructionFamily.NUMERIC_OP),
    I64XOR(0x85.toByte(), InstructionFamily.NUMERIC_OP),
    I64SHL(0x86.toByte(), InstructionFamily.NUMERIC_OP),
    I64SHRS(0x87.toByte(), InstructionFamily.NUMERIC_OP),
    I64SHRU(0x88.toByte(), InstructionFamily.NUMERIC_OP),
    I64ROTL(0x89.toByte(), InstructionFamily.NUMERIC_OP),
    I64ROTR(0x8A.toByte(), InstructionFamily.NUMERIC_OP),

    F32ABS(0x8B.toByte(), InstructionFamily.NUMERIC_OP),
    F32NEG(0x8C.toByte(), InstructionFamily.NUMERIC_OP),
    F32CEIL(0x8D.toByte(), InstructionFamily.NUMERIC_OP),
    F32FLOOR(0x8E.toByte(), InstructionFamily.NUMERIC_OP),
    F32TRUNC(0x8F.toByte(), InstructionFamily.NUMERIC_OP),
    F32NEAREST(0x90.toByte(), InstructionFamily.NUMERIC_OP),
    F32SQRT(0x91.toByte(), InstructionFamily.NUMERIC_OP),
    F32ADD(0x92.toByte(), InstructionFamily.NUMERIC_OP),
    F32SUB(0x93.toByte(), InstructionFamily.NUMERIC_OP),
    F32MUL(0x94.toByte(), InstructionFamily.NUMERIC_OP),
    F32DIV(0x95.toByte(), InstructionFamily.NUMERIC_OP),
    F32MIN(0x96.toByte(), InstructionFamily.NUMERIC_OP),
    F32MAX(0x97.toByte(), InstructionFamily.NUMERIC_OP),
    F32COPYSIGN(0x98.toByte()),

    F64ABS(0x99.toByte(), InstructionFamily.NUMERIC_OP),
    F64NEG(0x9A.toByte(), InstructionFamily.NUMERIC_OP),
    F64CEIL(0x9B.toByte(), InstructionFamily.NUMERIC_OP),
    F64FLOOR(0x9C.toByte(), InstructionFamily.NUMERIC_OP),
    F64TRUNC(0x9D.toByte(), InstructionFamily.NUMERIC_OP),
    F64NEAREST(0x9E.toByte(), InstructionFamily.NUMERIC_OP),
    F64SQRT(0x9F.toByte(), InstructionFamily.NUMERIC_OP),
    F64ADD(0xA0.toByte(), InstructionFamily.NUMERIC_OP),
    F64SUB(0xA1.toByte(), InstructionFamily.NUMERIC_OP),
    F64MUL(0xA2.toByte(), InstructionFamily.NUMERIC_OP),
    F64DIV(0xA3.toByte(), InstructionFamily.NUMERIC_OP),
    F64MIN(0xA4.toByte(), InstructionFamily.NUMERIC_OP),
    F64MAX(0xA5.toByte(), InstructionFamily.NUMERIC_OP),
    F64COPYSIGN(0xA6.toByte()),

    I32WRAPI64(0xA7.toByte(), InstructionFamily.CONVERSION_OP),
    I32TRUNCSF32(0xA8.toByte(), InstructionFamily.CONVERSION_OP),
    I32TRUNCUF32(0xA9.toByte(), InstructionFamily.CONVERSION_OP),
    I32TRUNCSF64(0xAA.toByte(), InstructionFamily.CONVERSION_OP),
    I32TRUNCUF64(0xAB.toByte(), InstructionFamily.CONVERSION_OP),
    I64EXTENDSI32(0xAC.toByte(), InstructionFamily.CONVERSION_OP),
    I64EXTENDUI32(0xAD.toByte(), InstructionFamily.CONVERSION_OP),
    I64TRUNCSF32(0xAE.toByte(), InstructionFamily.CONVERSION_OP),
    I64TRUNCSU32(0xAF.toByte(), InstructionFamily.CONVERSION_OP),
    I64TRUNCSF64(0xB0.toByte(), InstructionFamily.CONVERSION_OP),
    I64TRUNCSU64(0xB1.toByte(), InstructionFamily.CONVERSION_OP),

    F32CONVERTSI32(0xB2.toByte(), InstructionFamily.CONVERSION_OP),
    F32CONVERTUI32(0xB3.toByte(), InstructionFamily.CONVERSION_OP),
    F32CONVERTSI64(0xB5.toByte(), InstructionFamily.CONVERSION_OP),
    F32CONVERTUI64(0xB6.toByte(), InstructionFamily.CONVERSION_OP),
    F32DEMOTEF64(0xB6.toByte(), InstructionFamily.CONVERSION_OP),

    F64CONVERTSI32(0xB7.toByte(), InstructionFamily.CONVERSION_OP),
    F64CONVERTUI32(0xB8.toByte(), InstructionFamily.CONVERSION_OP),
    F64CONVERTSI64(0xB9.toByte(), InstructionFamily.CONVERSION_OP),
    F64CONVERTUI64(0xBA.toByte(), InstructionFamily.CONVERSION_OP),
    F64PROMOTEF32(0xBB.toByte(), InstructionFamily.CONVERSION_OP),

    I32REINTERPRETF32(0xBC.toByte(), InstructionFamily.CONVERSION_OP),
    I64REINTERPRETF64(0xBD.toByte(), InstructionFamily.CONVERSION_OP),
    F32REINTERPRETI32(0xBE.toByte(), InstructionFamily.CONVERSION_OP),
    F64REINTERPRETI64(0xBF.toByte(), InstructionFamily.CONVERSION_OP);

    companion object {
        fun fromOpcode(opCode: Byte) = try {
            values().first { it.opcode == opCode }
        } catch (e: NoSuchElementException) {
            throw RuntimeException("Unknown OpCode $opCode")
        }
    }
}

sealed class Instruction(open val type: InstructionType) {
    open fun sizeInBytes(): Long = TODO("Instruction of type $type")
}

data class VarInstruction(override val type: InstructionType, val index: Long) : Instruction(type) {
    override fun sizeInBytes(): Long = 1 + sizeInBytesOfU32(index)

}

data class I32ConstInstruction(val value: Long) : Instruction(InstructionType.I32CONST) {
    override fun sizeInBytes(): Long = 1 + sizeInBytesOfU32(value)
}

data class I64ConstInstruction(val value: Long) : Instruction(InstructionType.I64CONST)

data class F32ConstInstruction(val value: Float) : Instruction(InstructionType.F32CONST)
data class F64ConstInstruction(val value: Double) : Instruction(InstructionType.F64CONST)

class BlockInstruction(val blockType: BlockType, val content: List<Instruction>) : Instruction(InstructionType.BLOCK)

class LoopInstruction(val blockType: BlockType, val content: List<Instruction>) : Instruction(InstructionType.LOOP)

data class IfInstruction(val blockType: BlockType, val thenInstructions: List<Instruction>, val elseInstructions: List<Instruction>? = null) : Instruction(InstructionType.IF)

open class BinaryInstruction(type: InstructionType) : Instruction(type)

open class TestInstruction(type: InstructionType) : Instruction(type)

//
// Basic arithmetic instructions
//

object I32AddInstruction : BinaryInstruction(InstructionType.I32ADD)
object I32SubInstruction : BinaryInstruction(InstructionType.I32SUB)
object I32MulInstruction : BinaryInstruction(InstructionType.I32MUL)
object I32DivSInstruction : BinaryInstruction(InstructionType.I32DIVS)
object I32DivUInstruction : BinaryInstruction(InstructionType.I32DIVU)
object I32RemSInstruction : BinaryInstruction(InstructionType.I32REMS)
object I32RemUInstruction : BinaryInstruction(InstructionType.I32REMU)

object I64AddInstruction : BinaryInstruction(InstructionType.I64ADD)
object I64SubInstruction : BinaryInstruction(InstructionType.I64SUB)
object I64MulInstruction : BinaryInstruction(InstructionType.I64MUL)
object I64DivSInstruction : BinaryInstruction(InstructionType.I64DIVS)
object I64DivUInstruction : BinaryInstruction(InstructionType.I64DIVU)
object I64RemSInstruction : BinaryInstruction(InstructionType.I64REMS)
object I64RemUInstruction : BinaryInstruction(InstructionType.I64REMU)

object F32AddInstruction : BinaryInstruction(InstructionType.F32ADD)
object F32SubInstruction : BinaryInstruction(InstructionType.F32SUB)
object F32MulInstruction : BinaryInstruction(InstructionType.F32MUL)
object F32DivInstruction : BinaryInstruction(InstructionType.F32DIV)
object F32NegInstruction : BinaryInstruction(InstructionType.F32NEG)

object F64AddInstruction : BinaryInstruction(InstructionType.F64ADD)
object F64SubInstruction : BinaryInstruction(InstructionType.F64SUB)
object F64MulInstruction : BinaryInstruction(InstructionType.F64MUL)
object F64DivInstruction : BinaryInstruction(InstructionType.F64DIV)
object F64NegInstruction : BinaryInstruction(InstructionType.F64NEG)

//
// UnaryInstruction
//

open class UnaryInstruction(type: InstructionType) : Instruction(type)

object F32SqrtInstruction : UnaryInstruction(InstructionType.F32SQRT)
object F64SqrtInstruction : UnaryInstruction(InstructionType.F64SQRT)
object F32CeilInstruction : UnaryInstruction(InstructionType.F32CEIL)
object F64CeilInstruction : UnaryInstruction(InstructionType.F64CEIL)
object F32FloorInstruction : UnaryInstruction(InstructionType.F32FLOOR)
object F64FloorInstruction : UnaryInstruction(InstructionType.F64FLOOR)

object I32ShlInstruction : UnaryInstruction(InstructionType.I32SHL)
object I32ShruInstruction : UnaryInstruction(InstructionType.I32SHRU)
object I32ShrsInstruction : UnaryInstruction(InstructionType.I32SHRS)
object I32RotlInstruction : UnaryInstruction(InstructionType.I32ROTL)
object I32RotrInstruction : UnaryInstruction(InstructionType.I32ROTR)

object I64ShlInstruction : UnaryInstruction(InstructionType.I64SHL)
object I64ShruInstruction : UnaryInstruction(InstructionType.I64SHRU)
object I64ShrsInstruction : UnaryInstruction(InstructionType.I64SHRS)
object I64RotlInstruction : UnaryInstruction(InstructionType.I64ROTL)
object I64RotrInstruction : UnaryInstruction(InstructionType.I64ROTR)

//
// Comparison instructions
//


object I32EqzInstruction : TestInstruction(InstructionType.I32EQZ)

class BinaryComparison(type: InstructionType) : BinaryInstruction(type)

object I32AndInstruction : BinaryInstruction(InstructionType.I32AND)
object I32OrInstruction : BinaryInstruction(InstructionType.I32OR)
object I32XorInstruction : BinaryInstruction(InstructionType.I32XOR)

object I64AndInstruction : BinaryInstruction(InstructionType.I64AND)
object I64OrInstruction : BinaryInstruction(InstructionType.I64OR)
object I64XorInstruction : BinaryInstruction(InstructionType.I64XOR)

object F64PromoteF32Instruction : Instruction(InstructionType.F64PROMOTEF32)

object F32ConvertUI32Instruction : Instruction(InstructionType.F32CONVERTUI32)
object F32ConvertUI64Instruction : Instruction(InstructionType.F32CONVERTUI64)
object F32ConvertSI32Instruction : Instruction(InstructionType.F32CONVERTSI32)
object F32ConvertSI64Instruction : Instruction(InstructionType.F32CONVERTSI64)

object F64ConvertUI32Instruction : Instruction(InstructionType.F64CONVERTUI32)
object F64ConvertUI64Instruction : Instruction(InstructionType.F64CONVERTUI64)
object F64ConvertSI32Instruction : Instruction(InstructionType.F64CONVERTSI32)
object F64ConvertSI64Instruction : Instruction(InstructionType.F64CONVERTSI64)

object I32TruncSF32Instruction : Instruction(InstructionType.I32TRUNCSF32)
object I32TruncSF64Instruction : Instruction(InstructionType.I32TRUNCSF64)
object I32TruncUF32Instruction : Instruction(InstructionType.I32TRUNCUF32)
object I32TruncUF64Instruction : Instruction(InstructionType.I32TRUNCUF64)

object I32WrapI64Instruction : Instruction(InstructionType.I32WRAPI64)

object returnInstruction : Instruction(InstructionType.RETURN) {
    override fun toString() = "returnInstruction"
}

data class MemoryPosition(val align: Long, val offset: Long)

class MemoryInstruction(type: InstructionType, val memArg: MemoryPosition) : Instruction(type)

object GrowMem : Instruction(InstructionType.GROWMEM)
object CurrMem : Instruction(InstructionType.CURRMEM)

class JumpInstruction(val labelIndex: Long) : Instruction(InstructionType.CONDJUMP)
class ConditionalJumpInstruction(val labelIndex: Long) : Instruction(InstructionType.CONDJUMP)

class CallInstruction(val funcIndex: FuncIndex) : Instruction(InstructionType.CALL)
class IndirectCallInstruction(val typeIndex: TypeIndex) : Instruction(InstructionType.INDCALL)

object nop : Instruction(InstructionType.NOP)
object unreachable : Instruction(InstructionType.UNREACHABLE)
object drop : Instruction(InstructionType.DROP)
object select : Instruction(InstructionType.SELECT)