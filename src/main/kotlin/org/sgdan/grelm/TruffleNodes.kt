/**
 * Truffle nodes representing the AST data that
 * will be interpreted by GraalVM.
 */
package org.sgdan.grelm

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.DirectCallNode
import com.oracle.truffle.api.nodes.ExecutableNode
import com.oracle.truffle.api.nodes.RootNode

fun toNode(d: Declaration, elm: ElmLanguage): DeclarationNode {
    return DeclarationNode(elm, d.name, toNode(d.expr, elm))
}

fun toNode(e: Expression, elm: ElmLanguage): ExecutableNode =
        when (e) {
            is Unit -> UnitNode(elm)
            is Variable -> VariableNode(elm, e.name)
            is Argument -> ArgumentNode(elm, e.name)
            is List -> ListNode(elm, e.items.map { toNode(it, elm) }.toTypedArray())
            is Record -> RecordNode(
                    elm,
                    e.bind.keys.toTypedArray(),
                    e.bind.values.map { toNode(it, elm) }.toTypedArray())
            is Integer -> IntegerNode(elm, e.value)
            is Float -> FloatNode(elm, e.value)
            is Plus -> PlusNode(elm, toNode(e.e1, elm), toNode(e.e2, elm))
            is Lambda -> LambdaNode(elm, e.arg, toNode(e.body, elm))
            is Call -> CallNode(elm, toNode(e.fn, elm), toNode(e.arg, elm))
            else -> throw Exception("Unsupported expression: $e")
        }

class ListNode(private val elm: ElmLanguage,
               @Children val items: Array<ExecutableNode>
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame): Any {
        return items.map { it.execute(frame) }
    }
}

class RecordNode(private val elm: ElmLanguage,
                 private val keys: Array<String>,
                 @Children val values: Array<ExecutableNode>
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame): Map<String, Any> =
            keys.mapIndexed { index, s -> s to values[index].execute(frame) }.toMap()
}

class VariableNode(private val elm: ElmLanguage,
                   private val name: String
) : ExecutableNode(elm) {
    private val contextRef = lookupContextReference(ElmLanguage::class.java)
    override fun execute(frame: VirtualFrame): Any =
            when (val expr = contextRef.get().lookup(name)?.expr) {
                null -> throw UnsupportedOperationException("Variable $name not found")
                else -> expr.execute(frame)
            }
}

class ArgumentNode(private val elm: ElmLanguage,
                   private val name: String
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame): Any {
        // functions only take one argument, it should be
        // the only one in the frame
        return frame.arguments[0] ?: throw Exception("Argument $name not found")
    }
}

class PlusNode(private val elm: ElmLanguage,
               @Child var e1: ExecutableNode,
               @Child var e2: ExecutableNode
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame): Number {
        val r1 = e1.execute(frame)
        val r2 = e2.execute(frame)
        return when (r1) {
            is Double -> when (r2) {
                is Double -> r1 + r2
                is Long -> r1 + r2
                else -> throw Exception("Can't add: $r2")
            }
            is Long -> when (r2) {
                is Double -> r1 + r2
                is Long -> r1 + r2
                else -> throw Exception("Can't add: $r2")
            }
            else -> throw Exception("Can't add: $r1")
        }
    }
}

class IntegerNode(private val elm: ElmLanguage,
                  private val value: Long
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame) = value
}

class FloatNode(private val elm: ElmLanguage,
                private val value: Double
) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame) = value
}

class UnitNode(private val elm: ElmLanguage) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame) = "UNIT"
}

class LambdaNode(private val elm: ElmLanguage,
                 private val arg: String,
                 @Child var body: ExecutableNode) : ExecutableNode(elm) {
    override fun execute(frame: VirtualFrame): Any =
            body.execute(frame)
}

class CallNode(private val elm: ElmLanguage,
               @Child var fn: ExecutableNode,
               @Child var arg: ExecutableNode) : ExecutableNode(elm) {

    @Child
    private var dcNode: DirectCallNode = rt().createDirectCallNode(
            rt().createCallTarget(FunctionRootNode(elm, fn)))

    override fun execute(frame: VirtualFrame): Any {
        return dcNode.call(arg.execute(frame))
    }
}

class DeclarationNode(
        private val elm: ElmLanguage,
        val key: String,
        @Child var expr: ExecutableNode
) : RootNode(elm) {
    override fun execute(frame: VirtualFrame): Any {
        return expr.execute(frame)
    }
}

/**
 * Internal only, doesn't map to AST node from Elm
 */
class FunctionRootNode(
        private val elm: ElmLanguage,
        @Child var expr: ExecutableNode) : RootNode(elm) {
    override fun execute(frame: VirtualFrame): Any {
        return expr.execute(frame)
    }
}
