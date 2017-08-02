package me.tomassetti.wasmkit

import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*

class ReadingWasmFile {

    private fun loadAndStoreWorks(inputStream: InputStream) {
        val bytes = inputStream.readBytes()
        assertEquals(bytes, load(bytes).generateBytes())
    }

    private fun loadWorks(inputStream: InputStream) {
        val bytes = inputStream.readBytes()
        load(bytes)
    }

    @test
    fun readingWebDspCWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        loadWorks(inputStream)
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
