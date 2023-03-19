/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.repository

import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RepositoryIdTest {
  @Test
  fun create_whenInvalidName_throws() {
    assertFailsWith<IllegalArgumentException> {
      RepositoryId(name = "invalid:separator", url = URL("http://repo.git"))
    }
  }

  @Test
  fun parseFrom_whenValid_returnsRepositoryId() {
    assertEquals(
      expected = RepositoryId("repo.git", URL("http://repo.git")),
      actual = RepositoryId.parseFrom("repo.git:http://repo.git")
    )
  }

  @Test
  fun parseFrom_whenInvalidName_throws() {
    assertFailsWith<IllegalArgumentException> {
      RepositoryId.parseFrom("invalid*separator:http://repo.git")
    }
  }

  @Test
  fun parseFrom_whenInvalidUrl_throws() {
    assertFailsWith<IllegalArgumentException> {
      RepositoryId.parseFrom("invalid:separator:http://repo.git")
    }
  }
}
