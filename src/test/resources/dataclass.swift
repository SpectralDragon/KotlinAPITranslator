class Payload {
    public let data: Int
    public init(data: Int) {
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
class Node {
    public let data: Payload
    public var next: Node?
    public init(data: Payload, next: Node?) {
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
class Controller {
    public init() {
        fatalError("Method not implemented yet.")
    }
    public func foo() -> Node {
        return Node(data: bar(data: "5"), next: nil)
    }
    public func bar(data: Int) -> Payload {
        return Payload(data: data)
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