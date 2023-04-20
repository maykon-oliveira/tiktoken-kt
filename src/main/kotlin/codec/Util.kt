package codec

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.FileInputStream

fun loadCsv(url: String): Map<String, Int> {
    val inputStream = FileInputStream(url)
    return csvReader {
        skipEmptyLine = true
    }.readAll(inputStream)
        .associate {
            val (key, value) = it

            key to value.toInt()
        }
}
