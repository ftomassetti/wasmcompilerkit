package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.*
import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*

class ReadingWasmFile {

    private fun loadWorks(inputStream: InputStream) : WebAssemblyModule {
        val bytes = inputStream.readBytes()
        return load(bytes)
    }

    private fun interpretInstruction(byteArray: ByteArray) : Instruction {
        return readExpression(BytesReader(byteArray), delimiterExpected = false)
    }

    private fun interpretAndSerializeCode(codeBlock: CodeBlock) {
        val instructions = codeBlock.interpret()
        val serialized = serializeCodeBlock(instructions)
        assertEquals(codeBlock, serialized)
    }

    @test
    fun readingBlock() {
        val i = interpretInstruction(byteArrayOf(4, 64, 65, 0, 33, 2, 5, 15, 11))
        assertEquals(IfInstruction(emptyBlockType,
                listOf(I32ConstInstruction(0), VarInstruction(InstructionType.SET_LOCAL, 2)),
                listOf(returnInstruction)
                ), i)
    }

    @test
    fun readingGetLocal0() {
        interpretInstruction(byteArrayOf(32, 0))
    }

    @test
    fun readingGetLocal1() {
        interpretInstruction(byteArrayOf(32, 1))
    }

    @test
    fun readingGetLocal2() {
        interpretInstruction(byteArrayOf(32, 2))
    }

    @test
    fun readingGetLocal3() {
        interpretInstruction(byteArrayOf(32, 3))
    }

    @test
    fun readingTeeLocal() {
        interpretInstruction(byteArrayOf(34, 3))
    }

    @test
    fun readingI32Const0() {
        interpretInstruction(byteArrayOf(65, 0))
    }

    @test
    fun readingI32Const1() {
        interpretInstruction(byteArrayOf(65, 1))
    }

    @test
    fun readingI32Const2() {
        interpretInstruction(byteArrayOf(65, 2))
    }

    @test
    fun readingI32Const4() {
        interpretInstruction(byteArrayOf(65, 4))
    }

    @test
    fun readingI32Const15() {
        assertEquals(I32ConstInstruction(15), interpretInstruction(byteArrayOf(65, 15)))
    }

    @test
    fun readingI32Const10000() {
        assertEquals(I32ConstInstruction(10000), interpretInstruction(byteArrayOf(65, -112, -50, 0)))
    }

    @test
    fun writingI32Const15() {
        assertArrayEquals(byteArrayOf(65, 15), I32ConstInstruction(15).toBytes())
    }

    @test
    fun writingI32Const10000() {
        assertArrayEquals(byteArrayOf(65, -112, -50, 0), I32ConstInstruction(10000).toBytes())
    }

    @test
    fun readingI32ConstMinus16() {
        assertEquals(I32ConstInstruction(-16), interpretInstruction(byteArrayOf(65, 112)))
    }

    @test
    fun writingI32ConstMinus16() {
        assertArrayEquals(byteArrayOf(65, 112), I32ConstInstruction(-16).toBytes())
    }

    @test
    fun readingI32Add() {
        interpretInstruction(byteArrayOf(106, 32, 0, 32, 2))
    }

    @test
    fun readingI32Add2() {
        interpretInstruction(byteArrayOf(106, 44, 0, 0, 34, 3))
    }

    @test
    fun readingI32Add3() {
        interpretInstruction(byteArrayOf(106, 32, 3, 58, 0, 0))
    }

    @test
    fun readingI32Add4() {
        interpretInstruction(byteArrayOf(106, 34, 2, 32, 1))
    }

    @test
    fun readingI32Or() {
        interpretInstruction(byteArrayOf(114, 106, 32, 0, 32, 2, 106, 44, 0, 0, 34, 3))
    }

    @test
    fun readingI32Or2() {
        interpretInstruction(byteArrayOf(114, 106, 32, 3, 58, 0, 0, 32, 2))
    }

    @test
    fun readingLoad() {
        interpretInstruction(byteArrayOf(44, 0, 0))
    }

    @test
    fun readingStore() {
        interpretInstruction(byteArrayOf(58, 0, 0))
    }

//    @test
//    fun readingI32GtS() {
//        interpretInstruction(byteArrayOf(74, 4, 64, 65, 0, 33, 2, 5))
//    }

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
        loadWorks(inputStream).codeSection()!!.elements.forEach { interpretAndSerializeCode(it.code) }
    }

    @test
    fun readingDynamicWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingDynamicWasmFileCode() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics.wasm")
        loadWorks(inputStream).codeSection()!!.elements.forEach { interpretAndSerializeCode(it.code) }
    }

    @test
    fun readingDynamicCollWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-coll.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingDynamicCollWasmFileCode() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-coll.wasm")
        loadWorks(inputStream).codeSection()!!.elements.forEach { interpretAndSerializeCode(it.code) }
    }

    @test
    fun readingDynamicOptWasmFile() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-opt.wasm")
        loadWorks(inputStream)
    }

    @test
    fun readingDynamicOptWasmFileCode() {
        // obtained from https://d2jta7o2zej4pf.cloudfront.net/
        val inputStream = ReadingWasmFile::class.java.getResourceAsStream("/dynamics-opt.wasm")
        loadWorks(inputStream).codeSection()!!.elements.forEach { interpretAndSerializeCode(it.code) }
    }

}
