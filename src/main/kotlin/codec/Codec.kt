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
        var parts = Array(piece.size + 1) { arrayOf(it, Int.MAX_VALUE) }

        val getRank = fun(index: Int, skip: Int): Int {
            if (index + skip + 2 < parts.size) {
                val part = piece.sliceArray(parts[index][0] until parts[index + skip + 2][0])
                return vocabulary.getOrDefault(String(part), -1)
            }

            return -1
        }

        for (i in 0..parts.size - 2) {
            val rank = getRank(i, 0)

            if (rank >= 0) {
                parts[i][1] = rank
            }
        }

        while (parts.size > 1) {
            var minRank = Int.MAX_VALUE
            var minIndex = -1

            for (i in parts.indices) {
                if (parts[i][1] < minRank) {
                    minRank = parts[i][1]
                    minIndex = i
                }
            }

            if (minRank < Int.MAX_VALUE) {
                val i = minIndex
                val rank = getRank(i, 1)

                if (rank >= 0) {
                    parts[i][1] = rank
                } else {
                    parts[i][1] = Int.MAX_VALUE
                }

                if (i > 0) {
                    val rank1 = getRank(i - 1, 1)

                    if (rank1 >= 0) {
                        parts[i - 1][1] = rank1
                    } else {
                        parts[i - 1][1] = Int.MAX_VALUE
                    }
                }

                parts = parts.sliceArray(0 until i + 1) + parts.slice(i + 2 until parts.size)
            } else {
                break
            }
        }

        val ids = Array(parts.size - 1) { it }
        val tokens = Array(parts.size - 1) { it.toString() }

        for (i in ids.indices) {
            val token = String(piece.sliceArray(parts[i][0] until parts[i + 1][0]))
            tokens[i] = token
            ids[i] = vocabulary[token]!!
        }

        return Pair(ids, tokens)
    }
}