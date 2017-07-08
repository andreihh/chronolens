/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metanalysis.java

import org.junit.Test

import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.sourceFileOf

class JavaParserClassTest : JavaParserTest() {
    @Test fun `test class with supertypes`() {
        val source = """
        class IClass extends Object implements Comparable<IInterface> {
        }
        """
        val expected = sourceFileOf(Type(
                name = "IClass",
                supertypes = setOf("Object", "Comparable<IInterface>")
        ))
        assertEquals(expected, parser.parse(source))
    }

    @Test fun `test class with vararg parameter method`() {
        val source = """
        abstract class IClass {
            @Override
            abstract void println(String... args);
        }
        """
        val expected = sourceFileOf(Type(
                name = "IClass",
                modifiers = setOf("abstract"),
                members = setOf(Function(
                        signature = "println(String...)",
                        parameters = listOf(Variable(name = "args")),
                        modifiers = setOf("abstract", "@Override")
                ))
        ))
        assertEquals(expected, parser.parse(source))
    }
}
