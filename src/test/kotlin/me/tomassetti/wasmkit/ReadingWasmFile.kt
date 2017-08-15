package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.interpret
import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*

class ReadingWasmFile {

    private fun loadWorks(inputStream: InputStream) : WebAssemblyModule {
        val bytes = inputStream.readBytes()
        return load(bytes)
    }

    @test
    fun readingWebDspCWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingWebDspCWasmFileCode() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        loadWorks(inputStream).codeSection()!!.elements.forEach { it.code.interpret() }
    }

    @test
    fun readingDynamicWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingDynamicCollWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-coll.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingDynamicOptWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-opt.wasm")
        loadWorks(inputStream)
    }

}
