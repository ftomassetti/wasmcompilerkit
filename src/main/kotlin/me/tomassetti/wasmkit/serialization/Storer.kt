package me.tomassetti.wasmkit.serialization

import me.tomassetti.wasmkit.*

fun storeSection(abw: AdvancedBytesWriter, section: WebAssemblySection) {
    abw.writeByte(section.type.id)
    abw.writeU32(section.sizeInBytes())
    when (section.type) {
        SectionType.TYPE -> (section as WebAssemblyTypeSection).storeData(abw)
        else -> TODO("Serialization of section ${section.type}")
    }
}

fun WebAssemblyTypeSection.storeData(abw: AdvancedBytesWriter) {
    storeVector(abw, this.elements, { abw, e ->
        abw.writeByte(0x60)
        storeVector(abw, e.paramTypes, {abw, pt -> pt.storeData(abw) })
        storeVector(abw, e.returnTypes, {abw, rt -> rt.storeData(abw) })
    })
}

fun ValueType.storeData(abw: AdvancedBytesWriter) {
    TODO()
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
