/**
 * Contains data classes to represent the AST data that
 * will be turned into Truffle nodes.
 */
package org.sgdan.grelm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

val astContext = SerializersModule {
    polymorphic(Expression::class) {
        Unit::class with Unit.serializer()
        Variable::class with Variable.serializer()
        Argument::class with Argument.serializer()
        Integer::class with Integer.serializer()
        Float::class with Float.serializer()
        List::class with List.serializer()
        Record::class with Record.serializer()
        Plus::class with Plus.serializer()
        Lambda::class with Lambda.serializer()
        Call::class with Call.serializer()
    }
    polymorphic(Declaration::class) {
        Declaration::class with Declaration.serializer()
    }
}

interface NodeData
interface Expression : NodeData

@Serializable
@SerialName("decl")
data class Declaration(val name: String,
                       val expr: Expression) : NodeData {
}

@Serializable
@SerialName("unit")
data class Unit(private val placeholder: String = "") : Expression

@Serializable
@SerialName("var")
data class Variable(val name: String) : Expression

@Serializable
@SerialName("arg")
data class Argument(val name: String) : Expression

@Serializable
@SerialName("list")
data class List(val items: kotlin.collections.List<Expression>) : Expression

@Serializable
@SerialName("record")
data class Record(val bind: kotlin.collections.Map<String, Expression>) : Expression

@Serializable
@SerialName("int")
data class Integer(val value: Long) : Expression

@Serializable
@SerialName("float")
data class Float(val value: Double) : Expression

@Serializable
@SerialName("plus")
data class Plus(val e1: Expression, val e2: Expression) : Expression

@Serializable
@SerialName("lambda")
data class Lambda(val arg: String, val body: Expression) : Expression

@Serializable
@SerialName("call")
data class Call(val fn: Expression, val arg: Expression) : Expression
