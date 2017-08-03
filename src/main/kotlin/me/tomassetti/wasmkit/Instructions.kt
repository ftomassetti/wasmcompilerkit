package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.sizeInBytesOfU32

enum class InstructionFamily {
    UNSPECIFIED,
    VAR,
    NUMERIC_CONST
}

enum class InstructionType(val opcode: Byte, val family: InstructionFamily = InstructionFamily.UNSPECIFIED) {
    UNREACHABLE(0x00),
    NOP(0x01),
    BLOCK(0x02),
    LOOP(0x03),
    IF(0x04),
    JUMP(0x0C),
    CONDJUMP(0x0D),
    TABLEJUMP(0x0E),
    RETURN(0x0F),
    CALL(0x10),
    INDCALL(0x11),
    DROP(0x1A),
    SELECT(0x1B),

    GET_LOCAL(0x20, InstructionFamily.VAR),
    SET_LOCAL(0x21, InstructionFamily.VAR),
    TEE_LOCAL(0x22, InstructionFamily.VAR),
    GET_GLOBAL(0x23, InstructionFamily.VAR),
    SET_GLOBAL(0x24, InstructionFamily.VAR),

    LOADI32(0x28),
    LOADI64(0x29),
    LOADF32(0x2A),
    LOADF64(0x2B),
    LOADI32_8S(0x2C),
    LOADI32_8U(0x2D),
    LOADI32_16S(0x2E),
    LOADI32_16U(0x2F),
    LOADI64_8S(0x30),
    LOADI64_8U(0x31),
    LOADI64_16S(0x32),
    LOADI64_16U(0x33),
    LOADI64_32S(0x34),
    LOADI64_32U(0x35),
    STOREI32(0x36),
    STOREI64(0x37),
    STOREF32(0x38),
    STOREF64(0x39),
    STOREI32_8(0x3A),
    STOREI32_16(0x3B),
    STOREI64_8(0x3C),
    STOREI64_16(0x3D),
    STOREI64_32(0x3E),
    CURRMEM(0x3F),
    GROWMEM(0x40),
    I32CONST(0x41, InstructionFamily.NUMERIC_CONST),
    I64CONST(0x42, InstructionFamily.NUMERIC_CONST),
    F32CONST(0x43, InstructionFamily.NUMERIC_CONST),
    F64CONST(0x44, InstructionFamily.NUMERIC_CONST),
    I32EQZ(0x45),
    I32EQ(0x46),
    I32NE(0x47),
    I32LTS(0x48),
    I32LTU(0x49),
    I32GTS(0x4A),
    I32GTU(0x4B),
    I32LES(0x4C),
    I32LEU(0x4D),
    I32GES(0x4E),
    I32GEU(0x4F),
    I64EQZ(0x50),
    I64EQ(0x51),
    I64NE(0x52),
    I64LTS(0x53),
    I64LTU(0x54),
    I64GTS(0x55),
    I64GTU(0x56),
    I64LES(0x57),
    I64LEU(0x58),
    I64GES(0x59),
    I64GEU(0x5A),

    F32EQ(0x5B),
    F32NE(0x5C),
    F32LT(0x5D),
    F32GT(0x5E),
    F32LE(0x5F),
    F32GE(0x60),

    F64EQ(0x61),
    F64NE(0x62),
    F64LT(0x63),
    F64GT(0x64),
    F64LE(0x65),
    F64GE(0x66),

    I32CLZ(0x67),
    I32CTX(0x68),
    I32POPCNT(0x69),
    I32ADD(0x6A),
    I32SUB(0x6B),
    I32MUL(0x6C),
    I32DIVS(0x6D),
    I32DIVU(0x6E),
    I32REMS(0x6F),
    I32REMU(0x70),
    I32AND(0x71),
    I32OR(0x72),
    I32XOR(0x73),
    I32SHL(0x74),
    I32SHRS(0x75),
    I32SHRU(0x76),
    I32ROTL(0x77),
    I32ROTR(0x78),

    I64CLZ(0x79),
    I64CTX(0x7A),
    I64POPCNT(0x7B),
    I64ADD(0x7C),
    I64SUB(0x7D),
    I64MUL(0x7E),
    I64DIVS(0x7F),
    I64DIVU(0x80.toByte()),
    I64REMS(0x81.toByte()),
    I64REMU(0x82.toByte()),
    I64AND(0x83.toByte()),
    I64OR(0x84.toByte()),
    I64XOR(0x85.toByte()),
    I64SHL(0x86.toByte()),
    I64SHRS(0x87.toByte()),
    I64SHRU(0x88.toByte()),
    I64ROTL(0x89.toByte()),
    I64ROTR(0x8A.toByte()),

    F32ABS(0x8B.toByte()),
    F32NEG(0x8C.toByte()),
    F32CEIL(0x8D.toByte()),
    F32FLOOR(0x8E.toByte()),
    F32TRUNC(0x8F.toByte()),
    F32NEAREST(0x90.toByte()),
    F32SQRT(0x91.toByte()),
    F32ADD(0x92.toByte()),
    F32SUB(0x93.toByte()),
    F32MUL(0x94.toByte()),
    F32DIV(0x95.toByte()),
    F32MIN(0x96.toByte()),
    F32MAX(0x97.toByte()),
    F32COPYSIGN(0x98.toByte()),

    F64ABS(0x99.toByte()),
    F64NEG(0x9A.toByte()),
    F64CEIL(0x9B.toByte()),
    F64FLOOR(0x9C.toByte()),
    F64TRUNC(0x9D.toByte()),
    F64NEAREST(0x9E.toByte()),
    F64SQRT(0x9F.toByte()),
    F64ADD(0xA0.toByte()),
    F64SUB(0xA1.toByte()),
    F64MUL(0xA2.toByte()),
    F64DIV(0xA3.toByte()),
    F64MIN(0xA4.toByte()),
    F64MAX(0xA5.toByte()),
    F64COPYSIGN(0xA6.toByte()),

    I32WRAPI64(0xA7.toByte()),
    I32TRUNCSF32(0xA8.toByte()),
    I32TRUNCUF32(0xA9.toByte()),
    I32TRUNCSF64(0xAA.toByte()),
    I32TRUNCUF64(0xAB.toByte()),
    I64EXTENDSI32(0xAC.toByte()),
    I64EXTENDUI32(0xAD.toByte()),
    I64TRUNCSF32(0xAE.toByte()),
    I64TRUNCSU32(0xAF.toByte()),
    I64TRUNCSF64(0xB0.toByte()),
    I64TRUNCSU64(0xB1.toByte()),

    F32CONVERTSI32(0xB2.toByte()),
    F32CONVERTUI32(0xB3.toByte()),
    F32CONVERTSI64(0xB5.toByte()),
    F32CONVERTUI64(0xB6.toByte()),
    F32DEMOTEF64(0xB6.toByte()),

    F64CONVERTSI32(0xB7.toByte()),
    F64CONVERTUI32(0xB8.toByte()),
    F64CONVERTSI64(0xB9.toByte()),
    F64CONVERTUI64(0xBA.toByte()),
    F64PROMOTEF32(0xBB.toByte()),

    I32REINTERPRETF32(0xBC.toByte()),
    I64REINTERPRETF64(0xBD.toByte()),
    F32REINTERPRETI32(0xBE.toByte()),
    F64REINTERPRETI64(0xBF.toByte());

    companion object {
        fun fromOpcode(opCode: Byte) = values().first { it.opcode == opCode }
    }
}

abstract class Instruction(val type: InstructionType) {
    open fun sizeInBytes(): Long = TODO("Instruction of type $type")
}

class VarInstruction(type: InstructionType, val index: Long) : Instruction(type) {
    override fun sizeInBytes(): Long = 1 + sizeInBytesOfU32(index)

}
class I32ConstInstruction(type: InstructionType, val value: Long) : Instruction(type) {
    override fun sizeInBytes(): Long {
        return when (type) {
            InstructionType.I32CONST -> 1 + sizeInBytesOfU32(value)
            else -> TODO("Instruction of type $type")
        }
    }
}