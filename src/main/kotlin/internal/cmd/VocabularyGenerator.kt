package internal.cmd

import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.apache.commons.cli.*
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.*

data class Config(val mapName: String, val url: String, val filename: String)

val parser: CommandLineParser = DefaultParser()
val helper = HelpFormatter()
const val packageName = "codec"

fun main(args: Array<String>) {
    val options = Options()

    options.addOption("e", "encoding", true, "encoding format. (e.g. cl100k_base)")

    try {
        val cmd: CommandLine = parser.parse(options, args)

        if (!cmd.hasOption("e")) {
            return
        }

        val config: Config = getConfig(cmd.getOptionValue("e"))

        val path = Paths.get("src/main/kotlin/$packageName/${config.filename}")

        if (path.exists()) {
            path.deleteExisting()
        }

        Files.createFile(path)

        val file = path.toFile()
        val csvVocabularyPath = genCsvVocabulary(file.nameWithoutExtension, config.url)
        genKtVocabulary(file, csvVocabularyPath, config.mapName)

    } catch (ex: ParseException) {
        helper.printHelp("Tokenizer Java", options)
    }
}

fun genCsvVocabulary(filename: String, url: String): String {
    val httpClient = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
    val path = Paths.get("src/main/resources/${filename}.csv")

    if (path.exists()) {
        path.deleteExisting()
    }

    Files.createFile(path)

    val csvFile = path.toFile()

    val lines = httpClient
        .send(request, HttpResponse.BodyHandlers.ofLines())
        .body()
        .map {
            val parts = it.split(" ")

            if (parts.size != 2) {
                throw IllegalArgumentException("invalid line: $it")
            }

            val word = String(Base64.getDecoder().decode(parts[0]))

            listOf(word, parts[1])
        }
        .toList()

    csvWriter {
        quote {
            mode = WriteQuoteMode.ALL
            char = '"'
        }
        lineTerminator = "\n"
    }.open(csvFile) {
        writeRows(lines)
    }

    return path.toString().replace("\\", "/")
}

fun genKtVocabulary(file: File, csvVocabularyPath: String, mapName: String) {
    file.appendText("package $packageName\n\n")
    file.appendText("// THIS FILE WAS AUTOMATICALLY GENERATED. DO NOT MODIFY\n\n")

    file.appendText("val $mapName = fun () = loadCsv(\"${csvVocabularyPath}\")\n")
}

fun getConfig(encoding: String): Config {
    val configs = mapOf(
        "cl100k_base" to
                Config(
                    "cl100kBaseVocab",
                    "https://openaipublic.blob.core.windows.net/encodings/cl100k_base.tiktoken",
                    "cl100k_base_vocab.kt"
                ),
        "r50k_base" to
                Config(
                    "r50kBaseVocab",
                    "https://openaipublic.blob.core.windows.net/encodings/r50k_base.tiktoken",
                    "r50k_base_vocab.kt"
                ),
        "p50k_base" to
                Config(
                    "p50kBaseVocab",
                    "https://openaipublic.blob.core.windows.net/encodings/p50k_base.tiktoken",
                    "p50k_base_vocab.kt"
                ),
    )

    return configs[encoding] ?: throw IllegalArgumentException("config not found")
}
