package me.tomassetti.wasmkit.serialization

import me.tomassetti.wasmkit.*

fun storeSection(abw: AdvancedBytesWriter, section: WebAssemblySection) {
    abw.writeByte(section.type.id)
    abw.writeU32on5bytes(section.payloadSize())
    when (section.type) {
        SectionType.TYPE -> (section as WebAssemblyTypeSection).storeData(abw)
        SectionType.IMPORT -> (section as WebAssemblyImportSection).storeData(abw)
        SectionType.START -> (section as WebAssemblyStartSection).storeData(abw)
        SectionType.CODE -> (section as WebAssemblyCodeSection).storeData(abw)
        SectionType.CUSTOM -> (section as WebAssemblyCustomSection).storeData(abw)
        SectionType.DATA -> (section as WebAssemblyDataSection).storeData(abw)
        SectionType.ELEMENT -> (section as WebAssemblyElementSection).storeData(abw)
        SectionType.EXPORT -> (section as WebAssemblyExportSection).storeData(abw)
        SectionType.FUNCTION -> (section as WebAssemblyFunctionSection).storeData(abw)
        SectionType.GLOBAL -> (section as WebAssemblyGlobalSection).storeData(abw)
        SectionType.MEMORY -> (section as WebAssemblyMemorySection).storeData(abw)
        SectionType.TABLE -> (section as WebAssemblyTableSection).storeData(abw)
        else -> TODO("Serialization of section ${section.type}")
    }
}

private fun WebAssemblyImportSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        e.module.storeData(abw)
        e.name.storeData(abw)
        e.importData.storeData(abw)
    })
}

private fun ImportData.storeData(abw: AdvancedBytesWriter) {
    abw.writeByte(this.type().id)
    when (this) {
        is GlobalImportData -> {
            this.type.storeData(abw)
            this.mutability.storeData(abw)
        }
        is FunctionImportData -> {
            abw.writeU32(this.typeIndex)
        }
        is MemoryImportData -> {
            this.limits.storeData(abw)
        }
        is TableImportData -> {
            abw.writeByte(0x70)
            this.limits.storeData(abw)
        }
        else -> TODO()
    }
}

private fun ExportData.storeData(abw: AdvancedBytesWriter) {
    abw.writeByte(this.type().id)
    when (this) {
        is GlobalExportData -> {
            abw.writeU32(this.index)
        }
        is FunctionExportData -> {
            abw.writeU32(this.index)
        }
        is MemoryExportData -> {
            abw.writeU32(this.index)
        }
        is TableExportData -> {
            abw.writeU32(this.index)
        }
        else -> TODO()
    }
}

private fun Limits.storeData(abw: AdvancedBytesWriter) {
    if (this.max == null) {
        abw.writeByte(0x00)
        abw.writeU32(this.min)
    } else {
        abw.writeByte(0x01)
        abw.writeU32(this.min)
        abw.writeU32(this.max)
    }
}

private fun Boolean.storeData(abw: AdvancedBytesWriter) {
    if (this) {
        abw.writeByte(0x01)
    } else {
        abw.writeByte(0x00)
    }
}

private fun String.storeData(abw: AdvancedBytesWriter) {
    abw.writeU32(this.toByteArray().size.toLong())
    abw.writeBytes(this.toByteArray())
}

private fun WebAssemblyStartSection.storeData(abw: AdvancedBytesWriter) { TODO() }

private fun WebAssemblyCodeSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeU32on5bytes(e.payloadSize())
        storeVector(abw, e.locals, {abw, l ->
            abw.writeU32(l.first)
            abw.writeByte(l.second.id)
        })
        e.code.storeData(abw)
    })
}

private fun CodeBlock.storeData(abw: AdvancedBytesWriter) {
    abw.writeBytes(this.bytes)
}

private fun WebAssemblyCustomSection.storeData(abw: AdvancedBytesWriter) { TODO() }

private fun WebAssemblyExportSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        e.storeData(abw)
    })
}

fun ExportEntry.storeData(abw: AdvancedBytesWriter) {
    name.storeData(abw)
    exportData.storeData(abw)
}

private fun WebAssemblyElementSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeU32(e.tableIndex)
        e.expr.storeData(abw)
        abw.writeByte(0x0B)
        storeVector(abw, e.init, { abw, e ->
            abw.writeU32(e)
        })
    })
}

private fun WebAssemblyDataSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeU32(e.x)
        e.e.storeData(abw)
        abw.writeByte(0x0B)
        abw.writeU32(e.data.size.toLong())
        abw.writeBytes(e.data)
    })
}

private fun WebAssemblyFunctionSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeU32(e)
    })
}

private fun WebAssemblyMemorySection.storeData(abw: AdvancedBytesWriter) { TODO() }
private fun WebAssemblyTableSection.storeData(abw: AdvancedBytesWriter) { TODO() }

private fun WebAssemblyGlobalSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        e.globalType.storeData(abw)
        e.expr.storeData(abw)
        abw.writeByte(0x0B)
    })
}

private fun Instruction.storeData(abw: AdvancedBytesWriter) {
    abw.writeByte(this.type.opcode)
    when (this) {
        is VarInstruction -> abw.writeU32(this.index)
        is I32ConstInstruction -> abw.writeU32(this.value)
        else -> TODO("Instruction of type $type")
    }
}

private fun GlobalType.storeData(abw: AdvancedBytesWriter) {
    this.valueType.storeData(abw)
    this.mutability.storeData(abw)
}

fun WebAssemblyTypeSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeByte(0x60)
        storeVector(abw, e.paramTypes, {abw, pt -> pt.storeData(abw) })
        storeVector(abw, e.returnTypes, {abw, rt -> rt.storeData(abw) })
    })
}

fun ValueType.storeData(abw: AdvancedBytesWriter) {
    abw.writeByte(this.id)
}

fun <E> storeVector(abw: AdvancedBytesWriter, elements: List<E>, storeElement: (AdvancedBytesWriter, E) -> Any ) {
    abw.writeU32(elements.size.toLong())
    elements.forEach { storeElement(abw, it) }
}

fun storeModule(bytesWriter: BytesWriter, module: WebAssemblyModule) {
    val abw = AdvancedBytesWriter(bytesWriter)
    abw.writeBytes(MAGIC_NUMBER)
    abw.writeBytes(WebAssemblyVersion.WASM1.byteArray)
    module.sections.forEach { storeSection(abw, it) }
}
