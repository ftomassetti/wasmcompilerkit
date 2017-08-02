package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.WebAssemblyLoader
import java.io.InputStream

fun load(inputStream: InputStream) : WebAssemblyModule {
    return load(inputStream.readBytes())
}

fun load(bytes: ByteArray) : WebAssemblyModule {
    val module = WebAssemblyModule()
    val loader = WebAssemblyLoader(bytes, module)
    loader.readMagicNumber()
    module.version = loader.readVersion()
    while (!loader.hasFinished()) {
        loader.readSection()
        //throw RuntimeException("unread content: remaining bytes ${loader.remainingBytes()}")
    }
    return module
}
