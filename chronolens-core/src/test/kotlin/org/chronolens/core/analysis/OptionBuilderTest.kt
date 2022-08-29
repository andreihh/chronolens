/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.analysis

import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import org.chronolens.test.core.analysis.OptionsProviderBuilder
import org.junit.Test

class OptionBuilderTest {
    @Test
    fun nullable_whenNoName_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().description("simple int").nullable() }
    }

    @Test
    fun nullable_whenNoDescription_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().name("abc").nullable() }
    }

    @Test
    fun nullable_whenNoType_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails {
            optionsProvider.untypedOption<Int>().name("abc").description("simple int").nullable()
        }
    }

    @Test
    fun nullable_whenNotProvided_returnsNull() {
        val optionsProvider = OptionsProviderBuilder().build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .nullable()

        assertNull(option)
    }

    @Test
    fun nullable_whenNameProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .nullable()

        assertEquals(option, 5)
    }

    @Test
    fun nullable_whenAliasProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("a", 5).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .nullable()

        assertEquals(option, 5)
    }

    @Test
    fun nullable_whenBothNameAndAliasProvided_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 3).setOption("a", 5).build()

        assertFails {
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .nullable()
        }
    }

    @Test
    fun required_whenNoName_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().description("simple int").required() }
    }

    @Test
    fun required_whenNoDescription_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().name("abc").required() }
    }

    @Test
    fun required_whenNoType_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails {
            optionsProvider.untypedOption<Int>().name("abc").description("simple int").required()
        }
    }

    @Test
    fun required_whenNotProvided_throws() {
        val optionsProvider = OptionsProviderBuilder().build()

        assertFails {
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .required()
        }
    }

    @Test
    fun required_whenNameProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .required()

        assertEquals(option, 5)
    }

    @Test
    fun required_whenAliasProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("a", 5).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .required()

        assertEquals(option, 5)
    }

    @Test
    fun required_whenBothNameAndAliasProvided_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 3).setOption("a", 5).build()

        assertFails {
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .required()
        }
    }

    @Test
    fun default_whenNoName_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().description("simple int").defaultValue(3) }
    }

    @Test
    fun default_whenNoDescription_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails { optionsProvider.option<Int>().name("abc").defaultValue(3) }
    }

    @Test
    fun default_whenNoType_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 5).build()

        assertFails {
            optionsProvider
                .untypedOption<Int>()
                .name("abc")
                .description("simple int")
                .defaultValue(3)
        }
    }

    @Test
    fun default_whenNotProvided_returnsDefault() {
        val optionsProvider = OptionsProviderBuilder().build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .defaultValue(5)

        assertEquals(option, 5)
    }

    @Test
    fun default_whenNameProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 3).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .defaultValue(5)

        assertEquals(option, 3)
    }

    @Test
    fun default_whenAliasProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("a", 3).build()

        val option by
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .defaultValue(5)

        assertEquals(option, 3)
    }

    @Test
    fun default_whenBothNameAndAliasProvided_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", 3).setOption("a", 5).build()

        assertFails {
            optionsProvider
                .option<Int>()
                .name("abc")
                .alias("a")
                .description("simple int")
                .defaultValue(7)
        }
    }

    @Test
    fun repeated_whenNoName_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", listOf(2, 5)).build()

        assertFails { optionsProvider.option<Int>().description("list int").repeated() }
    }

    @Test
    fun repeated_whenNoDescription_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", listOf(2, 5)).build()

        assertFails { optionsProvider.option<Int>().name("abc").repeated() }
    }

    @Test
    fun repeated_whenNoType_throws() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", listOf(2, 5)).build()

        assertFails {
            optionsProvider.untypedOption<Int>().name("abc").description("list int").repeated()
        }
    }

    @Test
    fun repeated_whenNotProvided_returnsEmptyList() {
        val optionsProvider = OptionsProviderBuilder().build()

        val option by
            optionsProvider.option<Int>().name("abc").alias("a").description("list int").repeated()

        assertEquals(option, emptyList())
    }

    @Test
    fun repeated_whenNameProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("abc", listOf(2, 5)).build()

        val option by
            optionsProvider.option<Int>().name("abc").alias("a").description("list int").repeated()

        assertEquals(option, listOf(2, 5))
    }

    @Test
    fun repeated_whenAliasProvided_returnsValue() {
        val optionsProvider = OptionsProviderBuilder().setOption("a", listOf(2, 5)).build()

        val option by
            optionsProvider.option<Int>().name("abc").alias("a").description("list int").repeated()

        assertEquals(option, listOf(2, 5))
    }

    @Test
    fun repeated_whenBothNameAndAliasProvided_throws() {
        val optionsProvider =
            OptionsProviderBuilder()
                .setOption("abc", listOf(1, 3))
                .setOption("a", listOf(2, 5))
                .build()

        assertFails {
            optionsProvider.option<Int>().name("abc").alias("a").description("list int").repeated()
        }
    }
}
