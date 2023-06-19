class Foo {
    val mutableData: MutableList<String> = mutableListOf()

    val data: List<String>
        get() = mutableData.toList()
}