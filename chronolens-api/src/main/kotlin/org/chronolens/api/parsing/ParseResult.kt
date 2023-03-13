/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.parsing

import org.chronolens.model.SourceFile

/** The result of parsing a source file. */
public sealed class ParseResult {
  /**
   * Indicates the successful interpretation of a [SourceFile].
   *
   * @property source the interpreted source file
   */
  public data class Success(val source: SourceFile) : ParseResult()

  /**
   * Indicates that a [SourceFile] couldn't be interpreted due to syntax errors.
   *
   * @property cause the cause of the syntax error if known
   */
  public data class SyntaxError(val cause: SyntaxErrorException) : ParseResult()
}
