package me.tomassetti.wasmkit

import me.tomassetti.wasmkit.serialization.BytesReader
import me.tomassetti.wasmkit.serialization.emptyBlockType
import me.tomassetti.wasmkit.serialization.interpret
import me.tomassetti.wasmkit.serialization.readExpression
import java.io.InputStream
import org.junit.Test as test
import org.junit.Assert.*
import java.io.ByteArrayInputStream

class ReadingWasmFile {

    private fun loadWorks(inputStream: InputStream) : WebAssemblyModule {
        val bytes = inputStream.readBytes()
        return load(bytes)
    }

    private fun interpretInstruction(byteArray: ByteArray) : Instruction {
        return readExpression(BytesReader(byteArray), delimiterExpected = false)
    }

    @test
    fun readingBlock() {
        val i = interpretInstruction(byteArrayOf(4, 64, 65, 0, 33, 2, 5, 15, 11))
        println(i)
        assertEquals(IfInstruction(emptyBlockType,
                listOf(I32ConstInstruction(InstructionType.I32CONST, 0), VarInstruction(InstructionType.SET_LOCAL, 2)),
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
        loadWorks(inputStream).codeSection()!!.elements.forEach { it.code.interpret() }
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
        loadWorks(inputStream).codeSection()!!.elements.forEach { it.code.interpret() }
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
        loadWorks(inputStream).codeSection()!!.elements.forEach { it.code.interpret() }
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
        loadWorks(inputStream).codeSection()!!.elements.forEach { it.code.interpret() }
    }

}
