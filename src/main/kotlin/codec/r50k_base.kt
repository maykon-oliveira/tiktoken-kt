package codec

import java.util.regex.Pattern

fun newR50kBase(): Codec {
    return Codec(
        "r50k_base",
        r50kBaseVocab(),
        Pattern.compile("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+"),
        mapOf(
            "<|endoftext|>" to 50256,
        )
    )
}