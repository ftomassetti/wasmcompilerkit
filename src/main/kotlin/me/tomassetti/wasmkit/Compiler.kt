package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.MAGIC_NUMBER
import me.tomassetti.wasmkit.serialization.WebAssemblyLoader
import java.io.File
import java.io.InputStream
import java.util.*


fun main(args: Array<String>) {
    val module = WebAssemblyModule()
    File("example.wasm").writeBytes(module.toBytes())
}
