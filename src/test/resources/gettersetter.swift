public class GetterSetter {
    public var value: String {
        get {
            return "Hello JetBrains!"
        }
        set {
            println(message: "Set a new value " + "\(newValue)")
        }
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