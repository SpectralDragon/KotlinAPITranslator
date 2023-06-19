class Payload(val data: Long)

class Node(val data: Payload, var next: Node?)

class Controller {
    fun foo(): Node {
        return Node(bar(5), null)
    }

    fun bar(data: Long): Payload =
        Payload(data)
}