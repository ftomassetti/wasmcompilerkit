package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.AdvancedBytesWriter
import me.tomassetti.wasmkit.serialization.ToBytesArrayBytesWriter
import me.tomassetti.wasmkit.serialization.storeSection
import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*

class ReadingAndWritingWasmFile {

    private fun loadAndStoreWorks(inputStream: InputStream) {
        val bytes = inputStream.readBytes()
        assertEquals(bytes.toList(), load(bytes).toBytes().toList())
    }

    @test
    fun webDspCWasmFileTypeSectionSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).typeSection()!!.sizeInBytes()

        assertEquals(95, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeImportSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).importSection()!!.sizeInBytes()

        assertEquals(393, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeFunctionSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).functionSection()!!.sizeInBytes()

        assertEquals(47, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeGlobalSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).globalSection()!!.sizeInBytes()

        assertEquals(31, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeExportSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).exportSection()!!.sizeInBytes()

        assertEquals(377, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeElementSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).elementSection()!!.sizeInBytes()

        assertEquals(12, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeCodeSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).codeSection()!!.sizeInBytes()

        assertEquals(14145, sectionSize)
    }

    @test
    fun webDspCWasmFileTypeDataSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val sectionSize = load(bytes).dataSection()!!.sizeInBytes()

        assertEquals(69, sectionSize)
    }

    fun readingAndWritingSection(resourceName: String, start:Int, end:Int, sectionType: SectionType) {
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream(resourceName)
        val bytes = inputStream.readBytes()
        val sectionBytes = bytes.copyOfRange(start, end)

        val toBA = ToBytesArrayBytesWriter()
        val abw = AdvancedBytesWriter(toBA)
        storeSection(abw, load(bytes).sectionsByType(sectionType)[0])

        assertEquals(sectionBytes.toList(), toBA.bytes().toList())
    }

    @test
    fun readingAndWritingWebDspCWasmFileTypeSection() {
        readingAndWritingSection("/webdsp_c.wasm", 8, 109, SectionType.TYPE)
    }

    @test
    fun readingAndWritingWebDspCWasmFileImportSection() {
        readingAndWritingSection("/webdsp_c.wasm", 109, 508, SectionType.IMPORT)
    }

    @test
    fun readingAndWritingWebDspCWasmFileFunctionSection() {
        readingAndWritingSection("/webdsp_c.wasm", 508, 561, SectionType.FUNCTION)
    }

    @test
    fun readingAndWritingWebDspCWasmFileGlobalSection() {
        readingAndWritingSection("/webdsp_c.wasm", 561, 598, SectionType.GLOBAL)
    }

    @test
    fun readingAndWritingWebDspCWasmFileExportSection() {
        readingAndWritingSection("/webdsp_c.wasm", 598, 981, SectionType.EXPORT)
    }

    @test
    fun readingAndWritingWebDspCWasmFileElementSection() {
        readingAndWritingSection("/webdsp_c.wasm", 981, 999, SectionType.ELEMENT)
    }

    @test
    fun readingAndWritingWebDspCWasmFileCodeSection() {
        readingAndWritingSection("/webdsp_c.wasm", 999, 15150, SectionType.CODE)
    }

    @test
    fun readingAndWritingWebDspCWasmFileDataSection() {
        readingAndWritingSection("/webdsp_c.wasm", 15150, 15225, SectionType.DATA)
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
