package me.tomassetti.wasmkit.serialization

import me.tomassetti.wasmkit.*

fun storeSection(bytesWriter: BytesWriter, section: WebAssemblySection) {
    when (section.type) {
        else -> TODO()
    }
}

fun storeModule(bytesWriter: BytesWriter, module: WebAssemblyModule) {
    TODO()
}

