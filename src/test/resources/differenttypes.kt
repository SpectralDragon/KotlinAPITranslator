class Payload(
    val byte: Byte,
    val char: Char,
    val short: Short,
    val int: Int,
    val long: Long,
    val ubyte: UByte,
    val ushort: UShort,
    val uint: UInt,
    val ulong: ULong,
    val string: String,
    val list: List<String>,
    val mutableList: MutableList<String>,
    val map: Map<String, String>,
    val mutableMap: MutableMap<String, String>,
    val any: Any,
    val optionalString: String?
)