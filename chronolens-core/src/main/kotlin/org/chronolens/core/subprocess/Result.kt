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

package org.chronolens.core.subprocess

/** The result from executing a subprocess. */
sealed class Result {
    /**
     * The result of a successful execution of a subprocess.
     *
     * @property text the `UTF-8` encoded output from the `stdout` of the
     * subprocess
     */
    data class Success(val text: String) : Result()

    /**
     * The result of a subprocess which terminated with non-zero exit status.
     *
     * @property exitValue the exit code of the subprocess
     * @property message the `UTF-8` encoded output from the `stderr` of the
     * subprocess
     */
    data class Error(val exitValue: Int, val message: String) : Result()

    /**
     * Returns the output parsed from the `stdout` of a subprocess, or `null` if
     * the subprocess terminated abnormally.
     */
    fun getOrNull(): String? = (this as? Success)?.text

    /**
     * Returns the output parsed from the `stdout` of a subprocess.
     *
     * @throws SubprocessException if the subprocess terminated abnormally
     */
    fun get(): String = when (this) {
        is Success -> text
        is Error -> throw SubprocessException(exitValue, message)
    }

    /** Returns whether this result is a [Success]. */
    val isSuccess: Boolean
        get() = this is Success
}
