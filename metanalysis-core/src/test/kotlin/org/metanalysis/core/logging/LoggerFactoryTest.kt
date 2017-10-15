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

import org.junit.Test

import java.util.logging.LogManager

import kotlin.test.assertEquals

class LoggerFactoryTest {
    private val loggerFactory = LoggerFactory

    @Test fun `test loaded configuration applies changes`() {
        val format = LogManager.getLogManager()
                .getProperty("java.util.logging.SimpleFormatter.format")
        assertEquals(expected = "[%4\$s] %5\$s", actual = format)
    }

    @Test fun `test get logger with same name returns same instance`() {
        val expected = loggerFactory.getLogger<LoggerFactoryTest>()
        val actual = loggerFactory.getLogger<LoggerFactoryTest>()
        assertEquals(expected, actual)
    }
}
