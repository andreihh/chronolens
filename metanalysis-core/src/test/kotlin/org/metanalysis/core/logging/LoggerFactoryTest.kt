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

import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoggerFactoryTest {
    private val logger = LoggerFactory.getLogger<LoggerFactoryTest>()

    @Test fun `test load configuration from non-existent file throws`() {
        assertFailsWith<FileNotFoundException> {
            LoggerFactory.loadConfiguration("/non-existent.properties")
        }
    }

    @Test fun `test load configuration from relative file throws`() {
        assertFailsWith<IllegalArgumentException> {
            LoggerFactory.loadConfiguration("logging.properties")
        }
    }

    @Test fun `test load configuration applies changes`() {
        LoggerFactory.loadConfiguration("/logging.properties")
        val stream = ByteArrayOutputStream()
        val handler = StreamHandler(stream, SimpleFormatter())
        val message = "simple logger"
        logger.addHandler(handler)
        logger.info(message)
        handler.flush()
        assertEquals(message, stream.toString())
    }
}
