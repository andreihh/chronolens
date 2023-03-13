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
import org.chronolens.api.process.ProcessException
import org.chronolens.api.process.ProcessExecutor
import org.chronolens.api.process.ProcessExecutorProvider
import org.chronolens.api.process.ProcessResult

/** A subprocess executor. */
public class Subprocess : ProcessExecutorProvider {
  private val executor =
    newSingleThreadExecutor(
      object : ThreadFactory {
        private val defaultFactory = defaultThreadFactory()

        override fun newThread(r: Runnable): Thread {
          val thread = defaultFactory.newThread(r)
          thread.isDaemon = true
          return thread
        }
      },
    )

  private fun InputStream.readText(): String = reader().use(InputStreamReader::readText)

  private fun <T> submit(task: () -> T): Future<T> = executor.submit(task)

  override fun provide(directory: File): ProcessExecutor = ProcessExecutor { command ->
    var process: Process? = null
    try {
      process = ProcessBuilder().directory(directory).command(*command).start()
      process.outputStream.close()
      val error = submit { process.errorStream.readText() }
      val input = process.inputStream.readText()
      return@ProcessExecutor when (val exitValue = process.waitFor()) {
        0 -> ProcessResult.Success(input)
        // See http://tldp.org/LDP/abs/html/exitcodes.html for various
        // UNIX exit codes. See
        // http://man7.org/linux/man-pages/man7/signal.7.html for
        // various UNIX signal values. See
        // https://msdn.microsoft.com/en-us/library/cc704588.aspx for
        // more details regarding Windows exit codes.
        130,
        131,
        137,
        143,
        -1073741510 -> throw ProcessException(exitValue, "Subprocess killed!")
        else -> ProcessResult.Error(exitValue, error.get())
      }
    } catch (e: InterruptedException) {
      throw ProcessException(e)
    } catch (e: ExecutionException) {
      throw ProcessException(e)
    } catch (e: IOException) {
      throw ProcessException(e)
    } finally {
      process?.destroy()
    }
  }

  public companion object {
    /** Delegates to [execute] for the given [directory]. */
    @JvmStatic
    public fun execute(directory: File, vararg command: String): ProcessResult =
      ProcessExecutorProvider.INSTANCE.provide(directory).execute(*command)

    /** Delegates to [execute] for the current working directory. */
    @JvmStatic
    public fun execute(vararg command: String): ProcessResult =
      ProcessExecutorProvider.INSTANCE.provide(File(".")).execute(*command)
  }
}
