class Foo {
    var mutableData: NSMutableArray<String> = NSMutableArray()

    var data: Array<String> {
        return mutableData.toArray() as Array<String>
    }
}