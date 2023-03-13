/*
 * Copyright 2017-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

/** The result of executing a process. */
public sealed class ProcessResult {
  /**
   * The result of a successful execution of a process.
   *
   * @property output the `UTF-8` encoded output from the `stdout` of the process
   */
  public data class Success(val output: String) : ProcessResult()

  /**
   * The result of a process which terminated with non-zero exit status.
   *
   * @property exitValue the exit code of the process
   * @property message the `UTF-8` encoded output from the `stderr` of the process
   */
  public data class Error(val exitValue: Int, val message: String) : ProcessResult()

  /**
   * Returns the output parsed from the `stdout` of a process, or `null` if the process terminated
   * abnormally.
   */
  public fun getOrNull(): String? = (this as? Success)?.output

  /**
   * Returns the output parsed from the `stdout` of a process.
   *
   * @throws ProcessException if the process terminated abnormally
   */
  public fun get(): String =
    when (this) {
      is Success -> output
      is Error -> throw ProcessException(exitValue, message)
    }

  /** Returns whether this result is a [Success]. */
  public val isSuccess: Boolean
    get() = this is Success
}
