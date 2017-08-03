package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.sizeInBytesOfU32

enum class ImportType(val id: Byte) {
    FUNC(0x00),
    TABLE(0x01),
    MEM(0x02),
    GLOBAL(0x03);

    companion object {
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}


data class ImportEntry(val module: String, val name: String, val importData: ImportData) : Sized {
    override fun sizeInBytes(): Long {
        return 10 + module.toByteArray().size + name.toByteArray().size + importData.sizeInBytes()
    }

}

data class ExportEntry(val name: String, val exportData: ExportData) : Sized {
    override fun sizeInBytes(): Long {
        return 5 + name.toByteArray().size + exportData.sizeInBytes()
    }
}

abstract class ImportData : Sized {
    abstract fun type() : ImportType
}

data class GlobalImportData(val type: ValueType, val mutability: Boolean) : ImportData() {
    override fun sizeInBytes(): Long {
        return 2
    }

    override fun type() = ImportType.GLOBAL
}

data class FunctionImportData(val typeIndex: TypeIndex) : ImportData() {
    override fun type() = ImportType.FUNC

    override fun sizeInBytes(): Long {
        return sizeInBytesOfU32(typeIndex)
    }
}

data class MemoryImportData(val limits: Limits) : ImportData() {
    override fun type() = ImportType.MEM

    override fun sizeInBytes(): Long {
        return limits.sizeInBytes()
    }
}

data class TableImportData(val limits: Limits) : ImportData() {
    override fun type() = ImportType.TABLE

    override fun sizeInBytes(): Long {
        return limits.sizeInBytes() + 1
    }
}

abstract class ExportData : Sized {
    abstract fun type() : ImportType
}

data class GlobalExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.GLOBAL

    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class FunctionExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.FUNC

    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class MemoryExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.MEM

    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class TableExportData(val index: Long) : ExportData() {
    override fun type() = ImportType.TABLE

    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class TableDefinition(val limits: Limits) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class MemoryDefinition(val limits: Limits) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class GlobalType(val valueType: ValueType, val mutability: Boolean) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class GlobalDefinition(val globalType: GlobalType, val expr: Instruction) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class DataSegment(val x: MemIndex, val e: Instruction, val data: ByteArray) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

data class ElementSegment(val tableIndex: TableIndex, val expr: Instruction, val init: List<FuncIndex>) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}

class CodeBlock(val bytes: ByteArray)

data class CodeEntry(val locals: List<Pair<Long, ValueType>>, val code: CodeBlock) : Sized {
    override fun sizeInBytes(): Long {
        TODO()
    }
}
