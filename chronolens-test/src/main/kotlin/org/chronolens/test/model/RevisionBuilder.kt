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

package org.chronolens.test.model

import java.time.Instant
import org.chronolens.model.Revision
import org.chronolens.model.RevisionId
import org.chronolens.model.SourceTreeEdit
import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.apply

@BuilderMarker
public class RevisionBuilder(private val id: String) {
  public var date: Instant = Instant.now()
  public var author: String = "<unknown-author>"
  private val edits = mutableListOf<SourceTreeEdit>()

  public fun date(value: Instant): RevisionBuilder {
    date = value
    return this
  }

  public fun author(value: String): RevisionBuilder {
    author = value
    return this
  }

  public fun edit(edit: SourceTreeEdit): RevisionBuilder {
    +edit
    return this
  }

  public operator fun SourceTreeEdit.unaryPlus() {
    edits += this
  }

  public fun build(): Revision = Revision(RevisionId(id), date, author, edits)
}

public fun revision(id: String, init: Init<RevisionBuilder>): Revision =
  RevisionBuilder(id).apply(init).build()
