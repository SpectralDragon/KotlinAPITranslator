class Foo {
    var mutableData: NSMutableArray<String> = NSMutableArray()
    var data: Array<String> {
        return mutableData.toArray() as Array<String>
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