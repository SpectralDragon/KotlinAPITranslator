class GetterSetter {
    var value: String
        get() { return "Hello JetBrains!" }
        set(newValue) = println("Set a new value $newValue")
}