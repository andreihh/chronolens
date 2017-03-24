package org.metanalysis.core

import org.junit.Test

import org.metanalysis.core.Node.Function
import org.metanalysis.core.Node.Type
import org.metanalysis.core.Node.Variable
import org.metanalysis.test.assertEquals

class TypeTest {
    @Test fun `test find variable`() {
        val name = "version"
        val expected = Variable(name, listOf("1"))
        val type = Type(
                name = "IClass",
                members = setOf(
                        Type(name),
                        Function(name, emptyList()),
                        expected
                )
        )
        val actual = type.find<Variable>(name)
        assertEquals(expected, actual)
    }

    @Test fun `test find function`() {
        val name = "version"
        val expected = Function(name, emptyList(), "{\n  return 1;\n}")
        val type = Type(
                name = "IClass",
                members = setOf(
                        Type(name),
                        Variable(name),
                        expected
                )
        )
        val actual = type.find<Function>(name)
        assertEquals(expected, actual)
    }

    @Test fun `test find type`() {
        val name = "version"
        val expected = Type(name, setOf("Object"))
        val type = Type(
                name = "IClass",
                members = setOf(
                        Function(name, emptyList()),
                        Variable(name),
                        expected
                )
        )
        val actual = type.find<Type>(name)
        assertEquals(expected, actual)
    }
}
