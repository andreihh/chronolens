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

import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger

object LoggerFactory {
    private val formatter = object : Formatter() {
        override fun format(record: LogRecord): String = with(record) {
            "$loggerName: $level: $message${System.lineSeparator()}"
        }
    }

    @JvmStatic fun getLogger(name: String): Logger =
            Logger.getLogger(name).apply {
                useParentHandlers = false
                val handler = ConsoleHandler()
                handler.formatter = formatter
                addHandler(handler)
            }

    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java.name)
}
