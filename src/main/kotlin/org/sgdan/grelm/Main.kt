package org.sgdan.grelm

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException
import java.io.File

val context: Context by lazy { Context.create() }

fun execute(script: String): String =
        try {
            context.eval("elm", script)
        } catch (e: PolyglotException) {
            e
        }.toString()


/**
 * Run from command line
 */
fun main(args: Array<String>) {
    val filename = args[0]
    println("Loading JSON from file: $filename")
    val result = execute(File(filename).readText())
    println("Result: $result")
}