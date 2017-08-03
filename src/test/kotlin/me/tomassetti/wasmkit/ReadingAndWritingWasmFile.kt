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
        val section = load(bytes).typeSection()!!
        val sectionSize = section.sizeInBytes()

        assertEquals(101, sectionSize)
        assertEquals(95, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeImportSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).importSection()!!
        val sectionSize = section.sizeInBytes()

        assertEquals(399, sectionSize)
        assertEquals(393, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeFunctionSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).functionSection()!!

        assertEquals(53, section.sizeInBytes())
        assertEquals(47, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeGlobalSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).globalSection()!!

        assertEquals(37, section.sizeInBytes())
        assertEquals(31, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeExportSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).exportSection()!!

        assertEquals(383, section.sizeInBytes())
        assertEquals(377, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeElementSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).elementSection()!!

        assertEquals(18, section.sizeInBytes())
        assertEquals(12, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeCodeSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).codeSection()!!

        assertEquals(14151, section.sizeInBytes())
        assertEquals(14145, section.payloadSize())
    }

    @test
    fun webDspCWasmFileTypeDataSize() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/webdsp_c.wasm")
        val bytes = inputStream.readBytes()
        val section = load(bytes).dataSection()!!

        assertEquals(75, section.sizeInBytes())
        assertEquals(69, section.payloadSize())
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
