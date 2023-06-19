public class Foo {
    public let mutableData: NSMutableArray<String>
    public var data: NSArray<String> {
        return mutableData.toList()
    }
    public init() {
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