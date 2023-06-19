public class Payload {
    public let byte: Int8
    public let char: Int16
    public let short: Int16
    public let int: Int32
    public let long: Int
    public let ubyte: UInt8
    public let ushort: UInt16
    public let uint: UInt32
    public let ulong: UInt
    public let string: String
    public let list: NSArray<String>
    public let mutableList: NSMutableArray<String>
    public let map: NSDictionary<String, Any>
    public let mutableMap: NSMutableDictionary<String, Any>
    public let any: Any
    public let optionalString: String?
    public init(byte: Int8, char: Int16, short: Int16, int: Int32, long: Int, ubyte: UInt8, ushort: UInt16, uint: UInt32, ulong: UInt, string: String, list: NSArray<String>, mutableList: NSMutableArray<String>, map: NSDictionary<String, Any>, mutableMap: NSMutableDictionary<String, Any>, any: Any, optionalString: String?) {
        fatalError("Method not implemented yet.")
    }
    public func equals(other: Any?) -> Bool {
        fatalError("Method not implemented yet.")
    }
    public func hashCode() -> Int32 {
        fatalError("Method not implemented yet.")
    }
    public func toString() -> String {
        fatalError("Method not implemented yet.")
    }
}