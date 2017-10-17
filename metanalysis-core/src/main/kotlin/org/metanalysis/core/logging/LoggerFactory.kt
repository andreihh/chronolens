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

package org.metanalysis.core.logging

import java.util.logging.LogManager
import java.util.logging.Logger

/**
 * A utility factory for loggers which provides a default configuration.
 *
 * The default configuration logs to [System.err] with the following format:
 * `[%level] %message%n`. In order to override this configuration for newly
 * created loggers, you must call [LogManager.readConfiguration] after the
 * factory was initialized.
 */
object LoggerFactory {
    init {
        val resource = "logging.properties"
        val src = javaClass.getResourceAsStream(resource)
        LogManager.getLogManager().readConfiguration(src)
    }

    /**
     * Returns the logger instance with the given `name`, or a new instance if
     * no such logger exists.
     *
     * @param name the name of the requested logger
     * @return the requested logger
     */
    @JvmStatic
    fun getLogger(name: String): Logger = Logger.getLogger(name)

    /** Inline utility method. */
    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java.name)
}
