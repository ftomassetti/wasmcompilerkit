package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.Instruction
import me.tomassetti.wasmkit.Limits
import me.tomassetti.wasmkit.ValueType

enum class ImportType(val id: Byte) {
    FUNC(0x00),
    TABLE(0x01),
    MEM(0x02),
    GLOBAL(0x03);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}


data class ImportEntry(val module: String, val name: String, val importData: ImportData)

data class ExportEntry(val name: String, val exportData: ExportData)


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

data class TableDefinition(val limits: Limits)

data class MemoryDefinition(val limits: Limits)

data class GlobalType(val valueType: ValueType, val mutability: Boolean)

data class GlobalDefinition(val globalType: GlobalType, val expr: Instruction)

data class DataSegment(val x: MemIndex, val e: Instruction, val data: ByteArray)

data class ElementSegment(val tableIndex: TableIndex, val expr: Instruction, val init: List<FuncIndex>)

class CodeBlock(val bytes: ByteArray)

data class CodeEntry(val locals: List<Pair<Long, ValueType>>, val code: CodeBlock)
