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

public data class RepositoryId(public val vcs: String, public val url: URL) {
  public enum class Mode {
    LOCAL,
    REMOTE
  }

  public val type: Mode =
    when (url.protocol.lowercase()) {
      "file" -> Mode.LOCAL
      "http",
      "https" -> Mode.REMOTE
      else -> throw IllegalArgumentException("Invalid repository URL '$url'!")
    }

  public override fun toString(): String = "$vcs:$url"
}
