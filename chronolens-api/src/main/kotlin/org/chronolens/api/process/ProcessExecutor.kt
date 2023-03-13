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

package org.chronolens.api.process

/** A process executor. */
public fun interface ProcessExecutor {
  /**
   * Executes the given [command] in a subprocess in a set working directory and returns its result.
   *
   * The [command]'s output to `stdout` and `stderr` must be `UTF-8` encoded text.
   *
   * @return the parsed input from `stdout` (if the process terminated normally) or from `stderr`
   * (if the process terminated abnormally)
   * @throws ProcessException if the given [command] is invalid or if the current thread is
   * interrupted while waiting for the process to terminate or if any input related errors occur
   */
  public fun execute(vararg command: String): ProcessResult
}
