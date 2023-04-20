import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

internal class TokenizerTest {

    @TestFactory
    fun testCl100kEncoding() = listOf(
        "hello world" to listOf(15339, 1917),
        "hello  world" to listOf(15339, 220, 1917),
        "hello   world" to listOf(15339, 256, 1917),
        "supercalifragilistic" to listOf(13066, 3035, 278, 333, 4193, 321, 4633),
        "We know what we are, but know not what we may be." to listOf(
            1687,
            1440,
            1148,
            584,
            527,
            11,
            719,
            1440,
            539,
            1148,
            584,
            1253,
            387,
            13
        ),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("Encoding \"$input\" should output $expected") {
            val codec = Tokenizer.get(Encoding.Cl100kBase)
            val pair = codec.encode(input)
            assertEquals(expected, pair.first)
        }
    }
}