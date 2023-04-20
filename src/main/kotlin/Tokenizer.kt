import codec.Codec
import codec.newCl100kBase

enum class Encoding {
    Cl100kBase
}

class Tokenizer private constructor() {
    companion object {
        fun get(encoding: Encoding): Codec {
            val codec = when (encoding) {
                Encoding.Cl100kBase -> newCl100kBase()
            }

            return codec
        }
    }
}