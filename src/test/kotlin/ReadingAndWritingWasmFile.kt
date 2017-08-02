package me.tomassetti.wasmkit

import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*

class ReadingAndWritingWasmFile {

    private fun loadAndStoreWorks(inputStream: InputStream) {
        val bytes = inputStream.readBytes()
        assertEquals(bytes, load(bytes).generateBytes())
    }

    @test
    fun loadingWebDspCWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        loadAndStoreWorks(inputStream)
    }

    @test
    fun loadingDynamicWasmFile() {
        // obtained from https://github.com/guybedford/wasm-demo
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics.wasm")
        loadAndStoreWorks(inputStream)
    }

    @test
    fun loadingDynamicCollWasmFile() {
        // obtained from https://github.com/guybedford/wasm-demo
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-coll.wasm")
        loadAndStoreWorks(inputStream)
    }

    @test
    fun loadingDynamicOptFile() {
        // obtained from https://github.com/guybedford/wasm-demo
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-opt.wasm")
        loadAndStoreWorks(inputStream)
    }
}
