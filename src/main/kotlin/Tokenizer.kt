import codec.Codec
import codec.newCl100kBase
import codec.newR50kBase

enum class Encoding {
    CL100KBASE,
    R50KBASE
}

class Tokenizer private constructor() {
    companion object {
        fun get(encoding: Encoding): Codec {
            val codec = when (encoding) {
                Encoding.CL100KBASE -> newCl100kBase()
                Encoding.R50KBASE -> newR50kBase()
            }

            return codec
        }
    }
}