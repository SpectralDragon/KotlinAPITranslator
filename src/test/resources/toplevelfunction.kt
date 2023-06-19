fun foo() {
    println("foo")
}

fun bar(arg1: Int, arg2: Long): String {
    return "$arg1 $arg2"
}

fun baz(arg: String) =
    arg.lowercase()