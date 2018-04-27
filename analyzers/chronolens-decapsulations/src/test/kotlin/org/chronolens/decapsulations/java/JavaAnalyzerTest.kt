/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.decapsulations.java

import org.chronolens.core.model.Project
import org.chronolens.decapsulations.DecapsulationAnalyzer
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PACKAGE_LEVEL
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PRIVATE_LEVEL
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PRIVATE_MODIFIER
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PROTECTED_LEVEL
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PROTECTED_MODIFIER
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PUBLIC_LEVEL
import org.chronolens.decapsulations.java.JavaAnalyzer.Companion.PUBLIC_MODIFIER
import org.chronolens.test.core.model.project
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

// TODO: better tests
/*@Ignore
class JavaAnalyzerTest {
    private fun getProjectWithType(
        className: String,
        modifier: String? = null
    ): Project = project {
        sourceFile("$className.java") {
            type(className) {
                if (modifier != null) {
                    modifiers(modifier)
                }
            }
        }
    }

    private fun getProjectWithInterface(className: String): Project = project {
        sourceFile("$className.java") {
            type(className) {
                modifiers("interface")

                function("get()") {}
            }
        }
    }

    @Test fun `test private visibility`() {
        val name = "Main"
        val project = getProjectWithType(name, PRIVATE_MODIFIER)
        val id = "$name.java:$name"
        val expectedLevel = PRIVATE_LEVEL
        val actualLevel = DecapsulationAnalyzer.getVisibility(project, id)
        assertEquals(expectedLevel, actualLevel)
    }

    @Test fun `test package visibility`() {
        val name = "Main"
        val project = getProjectWithType(name)
        val id = "$name.java:$name"
        val expectedLevel = PACKAGE_LEVEL
        val actualLevel = DecapsulationAnalyzer.getVisibility(project, id)
        assertEquals(expectedLevel, actualLevel)
    }

    @Test fun `test protected visibility`() {
        val name = "Main"
        val project = getProjectWithType(name, PROTECTED_MODIFIER)
        val id = "$name.java:$name"
        val expectedLevel = PROTECTED_LEVEL
        val actualLevel = DecapsulationAnalyzer.getVisibility(project, id)
        assertEquals(expectedLevel, actualLevel)
    }

    @Test fun `test public visibility`() {
        val name = "Main"
        val project = getProjectWithType(name, PUBLIC_MODIFIER)
        val id = "$name.java:$name"
        val expectedLevel = PUBLIC_LEVEL
        val actualLevel = DecapsulationAnalyzer.getVisibility(project, id)
        assertEquals(expectedLevel, actualLevel)
    }

    @Test fun `test interface method is public`() {
        val name = "Main"
        val project = getProjectWithInterface(name)
        val id = "$name.java:$name:get()"
        val expectedLevel = PUBLIC_LEVEL
        val actualLevel = DecapsulationAnalyzer.getVisibility(project, id)
        assertEquals(expectedLevel, actualLevel)
    }
}*/
