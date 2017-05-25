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

import java.io.FileNotFoundException
import java.io.IOException
import java.util.logging.LogManager
import java.util.logging.Logger

object LoggerFactory {
    @Throws(IOException::class)
    @JvmStatic fun loadConfiguration(resource: String) {
        require(resource.startsWith("/")) { "'$resource' must be absolute!" }
        val src = LoggerFactory::class.java.getResourceAsStream(resource)
                ?: throw FileNotFoundException("'$resource' doesn't exist!")
        LogManager.getLogManager().readConfiguration(src)
    }

    @JvmStatic fun getLogger(name: String): Logger = Logger.getLogger(name)

    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java.name)
}
