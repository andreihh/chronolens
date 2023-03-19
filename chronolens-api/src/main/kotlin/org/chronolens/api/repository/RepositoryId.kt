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

import java.net.MalformedURLException
import java.net.URL

/**
 * A repository identifier comprising a unique [name] and the [url] where the repository is located.
 */
public data class RepositoryId(public val name: String, public val url: URL) {
  init {
    require(name.matches(NAME_REGEX)) { "Invalid repository name '$name'!" }
  }

  public override fun toString(): String = "$name$SEPARATOR$url"

  public companion object {
    private const val SEPARATOR: Char = ':'
    private val NAME_REGEX: Regex = Regex("[a-zA-Z0-9_\\-.]+")

    /**
     * Parses the given [rawRepositoryId].
     *
     * @throws IllegalArgumentException if the given [rawRepositoryId] is invalid
     */
    @JvmStatic
    public fun parseFrom(rawRepositoryId: String): RepositoryId {
      val (name, rawUrl) = rawRepositoryId.split(SEPARATOR, limit = 2)
      val url =
        try {
          URL(rawUrl)
        } catch (e: MalformedURLException) {
          throw IllegalArgumentException(e)
        }
      return RepositoryId(name, url)
    }
  }
}
