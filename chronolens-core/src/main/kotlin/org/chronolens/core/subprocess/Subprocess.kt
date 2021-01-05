/*
 * Copyright 2017-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors.defaultThreadFactory
import java.util.concurrent.Executors.newSingleThreadExecutor
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

/** A subprocess executor. */
public object Subprocess {
    private val builder = ProcessBuilder()
    private val executor = newSingleThreadExecutor(object : ThreadFactory {
        private val defaultFactory = defaultThreadFactory()

        override fun newThread(r: Runnable): Thread {
            val thread = defaultFactory.newThread(r)
            thread.isDaemon = true
            return thread
        }
    })

    private fun InputStream.readText(): String =
        reader().use(InputStreamReader::readText)

    private fun <T> submit(task: () -> T): Future<T> = executor.submit(task)

    /**
     * Executes the given [command] in a subprocess and returns its result.
     *
     * The [command]'s output to `stdout` and `stderr` must be `UTF-8` encoded
     * text.
     *
     * @return the parsed input from `stdout` (if the subprocess terminated
     * normally) or from `stderr` (if the subprocess terminated abnormally)
     * @throws SubprocessException if the given [command] is invalid or if the
     * current thread is interrupted while waiting for the subprocess to
     * terminate or if any input related errors occur
     */
    @JvmStatic
    public fun execute(directory: File, vararg command: String): Result {
        var process: Process? = null
        try {
            process =
                ProcessBuilder().directory(directory).command(*command).start()
            process.outputStream.close()
            val error = submit { process.errorStream.readText() }
            val input = process.inputStream.readText()
            val exitValue = process.waitFor()
            return when (exitValue) {
                0 -> Result.Success(input)
                // See http://tldp.org/LDP/abs/html/exitcodes.html for various
                // UNIX exit codes. See
                // http://man7.org/linux/man-pages/man7/signal.7.html for
                // various UNIX signal values. See
                // https://msdn.microsoft.com/en-us/library/cc704588.aspx for
                // more details regarding Windows exit codes.
                130, 131, 137, 143, -1073741510 ->
                    throw SubprocessException(exitValue, "Subprocess killed!")
                else -> Result.Error(exitValue, error.get())
            }
        } catch (e: InterruptedException) {
            throw SubprocessException(e)
        } catch (e: ExecutionException) {
            throw SubprocessException(e)
        } catch (e: IOException) {
            throw SubprocessException(e)
        } finally {
            process?.destroy()
        }
    }

    /** Delegates to [execute] for the current working directory. */
    public fun execute(vararg command: String): Result =
        execute(File("."), *command)
}
