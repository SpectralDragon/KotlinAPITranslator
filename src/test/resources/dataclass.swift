class Payload {
    public init(data: Int) {
        fatalError("")
    }
    let data: Int
    func equals(other: Any?) -> Bool {
        fatalError("")
    }
    func hashCode() -> Int32 {
        fatalError("")
    }
    func toString() -> String {
        fatalError("")
    }
}

class Node {
    public init(data: Payload, next: Node?) {
        fatalError("")
    }
    public let data: Payload
    public var next: Node?
        let data: Int
        func equals(other: Any?) -> Bool {
            fatalError("")
        }
        func hashCode() -> Int32 {
            fatalError("")
        }
        func toString() -> String {
            fatalError("")
        }
}

class Controller {
    public init() {
        fatalError("")
    }
    func foo() -> Node {
        return Node(payload: bar(5), next: nil)
    }
    func bar(data: Int) -> Payload {
        return Payload(data: data)
    }
    func equals(other: Any?) -> Bool {
        fatalError("")
    }
    func hashCode() -> Int32 {
        fatalError("")
    }
    func toString() -> String {
        fatalError("")
    }
}