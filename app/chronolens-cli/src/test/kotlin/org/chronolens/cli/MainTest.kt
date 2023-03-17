/*
 * Copyright 2022-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.chronolens.api.versioning.VcsRevision
import org.chronolens.test.api.versioning.FakeVcsProxyFactory
import org.chronolens.test.api.versioning.vcsRevision
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class MainTest {
  @get:Rule val tmp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

  private val directory by lazy { tmp.root }
  private val directoryPath by lazy { directory.absolutePath }
  private val out = ByteArrayOutputStream()

  @BeforeTest
  fun setStdOut() {
    System.setOut(PrintStream(out))
  }

  @Test
  fun main_whenRevList_printsRevisions() {
    val vcsProxy =
      FakeVcsProxyFactory.createRepository(directory) {
        +vcsRevision { change("README.md", "Hello, world!") }
        +vcsRevision { delete("README.md") }
      }
    val expected =
      vcsProxy.getHistory().joinToString(separator = "\n", transform = VcsRevision::id) + "\n"

    main("rev-list", "--repository-root", directoryPath)
    val actual = out.toString()

    assertEquals(expected, actual)
  }

  @Test
  fun main_whenLsTree_printsSources() {
    FakeVcsProxyFactory.createRepository(directory) {
      +vcsRevision {
        change("README.md", "Hello, world!")
        change("BUILD", "")
        change("src/Main.java", "")
        change("src/Test.java", "")
      }
    }
    val expected =
      """
            src/Main.java
            src/Test.java

            """
        .trimIndent()

    main("ls-tree", "--repository-root", directoryPath)
    val actual = out.toString()

    assertEquals(expected, actual)
  }

  @Test
  fun main_whenModel_printsSourceNode() {
    FakeVcsProxyFactory.createRepository(directory) {
      +vcsRevision {
        change("src/Main.java", "")
        change("src/Test.java", "")
      }
    }
    val expected =
      """
            file src/Main.java

            """
        .trimIndent()

    main("model", "--repository-root", directoryPath, "--qualified-id", "src/Main.java")
    val actual = out.toString()

    assertEquals(expected, actual)
  }
}
