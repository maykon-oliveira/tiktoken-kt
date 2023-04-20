package codec

import java.util.regex.Pattern

data class Part(val offset: Int, var rank: Int)

class Codec(
    val name: String,
    private val vocabulary: Map<String, Int>,
    private val splitRegex: Pattern,
    private val specialTokens: Map<String, Int>,
    private var reverseVocabulary: Map<Int, String>? = null,
) {
    fun encode(input: String): Pair<List<Int>, List<String>> {
        val ids = arrayListOf<Int>()
        val tokens = arrayListOf<String>()
        val matcher = splitRegex.matcher(input)

        while (matcher.find()) {
            val piece = matcher.group()
            val id = vocabulary[piece]

            if (id != null) {
                ids.add(id)
                tokens.add(piece)
            } else {
                val (newIds, newTokens) = bpe(piece.toByteArray())
                ids.addAll(newIds)
                tokens.addAll(newTokens)
            }

        }

        return Pair(ids, tokens)
    }

    fun decode(tokens: IntArray): String {
        if (reverseVocabulary == null) {
            reverseVocabulary = vocabulary.entries.associateBy({ it.value }) { it.key }
        }

        var out = ""

        for ((i, t) in tokens.withIndex()) {
            val piece = reverseVocabulary!![t]

            if (piece != null) {
                out += piece
            }
        }

        return out
    }

    private fun bpe(piece: ByteArray): Pair<Array<Int>, Array<String>> {
        var parts = Array(piece.size + 1) { Part(it, Int.MAX_VALUE) }

        val getRank = fun(index: Int, skip: Int): Int {
            if (index + skip + 2 < parts.size) {
                val start = parts[index].offset
                val end = parts[index + skip + 2].offset

                val rank = vocabulary[String(piece.slice(start until end).toByteArray())]

                if (rank != null) {
                    return rank
                }
            }

            return Int.MAX_VALUE
        }

        for (i in 0..parts.size - 2) {
            parts[i].rank = getRank(i, 0)
        }

        while (true) {
            if (parts.size == 1) {
                break
            }

            var minRank = Int.MIN_VALUE
            var minIndex = 0

            for (i in parts.indices) {
                val p = parts[i]

                if (p.rank < minRank) {
                    minRank = p.rank
                    minIndex = i
                }
            }

            if (minRank == Int.MAX_VALUE) {
                break
            }

            parts[minIndex].rank = getRank(minIndex, 1)

            if (minIndex > 0) {
                parts[minIndex - 1].rank = getRank(minIndex - 1, 1)
            }

            parts = (parts.slice(0 until minIndex + 1) + parts.slice(minIndex + 2 until parts.size)).toTypedArray()
        }

        val ids = Array(parts.size - 1) { it }
        val tokens = Array(parts.size - 1) { it.toString() }

        for (i in ids.indices) {
            val token = String(piece.slice(parts[i].offset until parts[i + 1].offset).toByteArray())
            tokens[i] = token
            ids[i] = vocabulary[token]!!
        }

        return Pair(ids, tokens)
    }
}