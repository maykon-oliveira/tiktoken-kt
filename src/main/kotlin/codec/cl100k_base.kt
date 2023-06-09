package codec

import java.util.regex.Pattern

fun newCl100kBase(): Codec {
    return Codec(
        "cl100k_base",
        cl100kBaseVocab(),
        Pattern.compile("(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}{1,3}| ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+"),
        mapOf(
            "<|endoftext|>" to 100257,
            "<|fim_prefix|>" to 100258,
            "<|fim_middle|>" to 100259,
            "<|fim_suffix|>" to 100260,
            "<|endofprompt|>" to 100276,
        )
    )
}