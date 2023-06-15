# Kotlin API Translator test assignment

The task is to implement a Kotlin compiler plugin that generates a developer-friendly Swift API for given Kotlin sources
and cover it with tests.
You don't have to support all language features or translate method bodies.
To make generated Swift code correct, you can use something like `fatalError()` as a placeholder for method bodies.
For example, Kotlin function

```kotlin
fun foo() {
    println("foo")
}
```

Should translate to something like

```swift
func foo() {
    fatalError()
}
```

## Implementation

Use this template project to avoid spending time on complicated setup and boilerplate.
The starting point for your translator is `src/main/kotlin/KotlinApiTranslatorVisitor.kt`.
It contains an initial implementation of a Kotlin IR visitor.
Your goal is to generate Swift source files in the `KotlinApiTranslatorGenerationExtension.outputDirectory`.

Use the test infrastructure (see to `src/test/kotlin/SampleTest.kt` for an example) to write test cases and verify your
solution.

Feel free to use any 3rd party libraries, except those that provide a simpler compiler API (e.g., KSP).
The goal is to test your ability to deal with complex, undocumented API, like it is going to be in a real project.

We recognize that you may encounter cases where you will feel stuck or don't know which approach is the right one.
That's totally OK!  
Write those cases down, think of possible workarounds, and we will discuss them.

## Tasks

### Notes

* Tasks 1-3 assume that all declarations belong to a single root package (so not `package ...` at the top of the file).
* Try to make the resulting Swift API as convenient as possible for Swift developers.

### 1. Top-level functions with primitive types

Let's start with something basic. Support top-level functions in your translator that work with the following types:

1. `Boolean`
2. `Byte`
3. `Short`
4. `Int`
5. `Long`
6. `Float`
7. `Double`
8. `String`
9. `Unit` as a return type

Example:

```kotlin
fun foo() {
    println("foo")
}

fun bar(arg1: Int, arg2: Long): String {
    return "$arg1 $arg2"
}

fun baz(arg: String) =
        arg.lowercase()
```

### 2. Final classes

Now we will add a bit of OOP to your translator: classes.
Let's keep things relatively simple: you need to support only `final` classes, and avoid supporting inheritance and
other types of classes (`enum class`/`object`/`interface`/etc).

Example:

```kotlin
class Payload(val data: Long)

class Node(val data: Payload, var next: Node?)

class Controller {
    fun foo(): Node {
        return Node(bar(5), null)
    }

    fun bar(data: Long): Payload =
            Payload(data)
}
```

### 3. `List<String>` and `MutableList<String>` types

Add support for `List<String>` and `MutableList<String>` types to your translator.
Developers work with collection types almost every day, so it is important to expose them to Swift in a nice and
predictable way.

Note that you don't need to support arbitrary generic arguments, `String` type will be sufficient for this task.

Example:

```kotlin
class Foo {
    val mutableData: MutableList<String> = mutableListOf()

    val data: List<String>
        get() = mutableData.toList()
}
```

### 4. Multiple packages

The final task is to remove the single package restriction.

Example:

```kotlin
// file1.kt
package a.b.c

class Foo()
```

```kotlin
// file2.kt
package b.c.d

class Bar()
```

## Meta

### Expected result

* A link to a repository or an archive containing the solution.
* (Optional) A list of problems you encountered.

### Deadlines

* For the sake of our (and your :) ) sanity, we kindly ask you to spend not more than 16-20 working hours on this
  test assignment.
* There’s no strict deadline, you can either spend those 16 hours in “hackathon mode” over the weekend, or two hours a
  day over a week. Either way, please let us know your rough ETA.

### Things we'll evaluate

Note: You’re not expected to implement each and every point listed below flawlessly! We understand that the time
constraints are harsh and thus prepared to see you taking tough tradeoffs. We'll be curious to hear how you chose them,
though! :)

* How idiomatic is the generated Swift API.
* How edge cases are handled.
* The code quality of the solution: architecture, readability, flexibility for further evolution, tests, etc.

### Contacts

Feel free to ask questions in the email thread where you’ve received the test assignment.