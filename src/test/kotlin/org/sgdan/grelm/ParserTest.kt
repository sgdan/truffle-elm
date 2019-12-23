package org.sgdan.grelm

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

private val json = Json(context = astContext)
private val expressions = PolymorphicSerializer(Expression::class)

class ParserTest : StringSpec({
    "parse declaration" {
        val src = "{\"type\":\"decl\",\"name\":\"X\$foo\",\"expr\":{\"type\":\"int\",\"value\":2}}"
        json.parse(decls, src) shouldBe
                Declaration(name = "X\$foo", expr = Integer(value = 2))
    }
    "parse variable" {
        val src = "{ \"type\": \"var\", \"name\": \"X\$foo\" }"
        json.parse(expressions, src) shouldBe
                Variable(name = "X\$foo")
    }
    "parse unit" {
        json.parse(expressions, "{ \"type\": \"unit\" }") shouldBe Unit()
    }
    "parse list" {
        val src = """
            {
                "type": "list",
                "items": [
                    { "type": "int", "value": 5 },
                    { "type": "float", "value": -6.3 }
                ]
            }
        """.trimIndent()
        json.parse(expressions, src) shouldBe
                List(items = listOf(Integer(5), Float(-6.3)))
    }
    "parse record" {
        val src = """
        {
          "type": "record",
          "bind": {
            "x": { "type": "int", "value": 4 },
            "y": { "type": "unit" },
            "z": { "type": "int", "value": -5 }
          }
        }
        """.trimIndent().replace('|', '$')
        json.parse(expressions, src) shouldBe
                Record(bind = mapOf(
                        "x" to Integer(value = 4),
                        "y" to Unit(),
                        "z" to Integer(value = -5)
                ))
    }

    "generate int" {
        json.stringify(expressions, Integer(6)) shouldBe
                """{"type":"int","value":6}"""
    }
    "generate decl" {
        json.stringify(decls,
                Declaration("test", Float(6.4))) shouldBe
                """{"type":"decl","name":"test","expr":{"type":"float","value":6.4}}"""
    }
})
