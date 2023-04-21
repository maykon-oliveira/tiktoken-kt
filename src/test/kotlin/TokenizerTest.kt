import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

internal class TokenizerTest {

    @TestFactory
    fun `test cl100kbase encoding`() = listOf(
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
            val codec = Tokenizer.get(Encoding.CL100KBASE)
            val pair = codec.encode(input)
            assertEquals(expected, pair.first)
        }
    }

    @TestFactory
    fun `test r50kbase encoding`() = listOf(
        "hello world" to listOf(31373, 995),
        "hello  world" to listOf(31373, 220, 995),
        "hello   world" to listOf(31373, 220, 220, 995),
        "supercalifragilistic" to listOf(16668, 9948, 361, 22562, 346, 2569),
        "We know what we are, but know not what we may be." to listOf(
            1135,
            760,
            644,
            356,
            389,
            11,
            475,
            760,
            407,
            644,
            356,
            743,
            307,
            13
        ),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("Encoding \"$input\" should output $expected") {
            val codec = Tokenizer.get(Encoding.R50KBASE)
            val pair = codec.encode(input)
            assertEquals(expected, pair.first)
        }
    }
}