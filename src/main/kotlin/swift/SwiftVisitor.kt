package swift

abstract class SwiftVisitor {

    open fun visitElement(element: SwiftElement) { }

    open fun visitExpression(expression: SwiftExpression) {
        visitElement(expression)
    }

    open fun visitProperty(property: SwiftProperty) {
        visitElement(property)
    }

    open fun visitFunction(function: SwiftFunction) {
        visitElement(function)
    }

    open fun visitFunctionParameter(parameter: SwiftFunctionParameter) {
        visitElement(parameter)
    }

    open fun visitClass(declaration: SwiftClass) {
        visitElement(declaration)
    }

    open fun visitConst(const: SwiftConst) {
        visitElement(const)
    }

    open fun visitGetValue(value: SwiftGetValue) {
        visitElement(value)
    }

    open fun visitFunctionCall(expression: SwiftFunctionCall) {
        visitElement(expression)
    }

    open fun visitBlock(body: SwiftBlock) {
        visitElement(body)
    }

    open fun visitReturn(expression: SwiftReturn) {
        visitElement(expression)
    }

    open fun visitLiteral(literal: SwiftLiteral) {
        visitElement(literal)
    }
}