package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.ToBytesArrayBytesWriter
import me.tomassetti.wasmkit.serialization.WebAssemblyLoader
import me.tomassetti.wasmkit.serialization.storeModule
import java.io.InputStream
import java.io.OutputStream

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
    }
    return module
}

fun WebAssemblyModule.storeData(outputStream: OutputStream) {
    outputStream.write(this.toBytes())
}

fun WebAssemblyModule.toBytes() : ByteArray {
    val bytesWriter = ToBytesArrayBytesWriter()
    storeModule(bytesWriter, this)
    return bytesWriter.bytes()
}