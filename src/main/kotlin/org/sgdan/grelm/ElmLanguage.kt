/**
 * GraalVM language representing Elm (will be interpreted
 * from the AST supplied in JSON format).
 */
package org.sgdan.grelm

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.Truffle
import com.oracle.truffle.api.TruffleLanguage
import com.oracle.truffle.api.TruffleLanguage.Registration
import com.oracle.truffle.api.TruffleRuntime
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

fun rt(): TruffleRuntime = Truffle.getRuntime()

class ElmContext {
    private val declarations = HashMap<String, DeclarationNode>()

    fun register(name: String, dn: DeclarationNode) {
        declarations[name] = dn
    }

    fun lookup(name: String) = declarations[name]
}

val decls = PolymorphicSerializer(Declaration::class)

@Registration(id = "elm", name = "Elm", version = "0.1")
class ElmLanguage : TruffleLanguage<ElmContext>() {

    override fun createContext(env: Env) = ElmContext()

    override fun isObjectOfLanguage(`object`: Any): Boolean = false

    override fun parse(request: ParsingRequest): CallTarget {
        val src = request.source.characters.toString()
        val declarations = Json(context = astContext)
                // convert JSON into data classes
                .parse(decls.list, src)

                // convert data classes into truffle AST nodes
                .map { toNode(it, this) }

                // map the root nodes by lookup name
                .associateBy({ it.key }, { it })

        // register declarations
        declarations.forEach {
            getCurrentContext(ElmLanguage::class.java).register(it.key, it.value)
        }

        val main: DeclarationNode = declarations["Main\$main"]
                ?: throw Exception("No Main\$main node found")
        return rt().createCallTarget(main)
    }
}
