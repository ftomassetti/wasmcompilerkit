import spark.Service
import spark.kotlin.Http
import java.io.File

fun main(args: Array<String>) {
    val http = Http(Service.ignite().port(9000))
    http.get("/") {
        File("example.html").readText()
    }
    http.get("/example.wasm") {
        File("example.wasm").readBytes()
    }
}
