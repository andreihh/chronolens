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

package org.chronolens.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.MultipleOption
import kotlinx.cli.SingleNullableOption
import kotlinx.cli.SingleOption
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.cli.required
import org.chronolens.core.analysis.Option
import org.chronolens.core.analysis.OptionsProvider
import org.chronolens.core.analysis.option
import java.io.File
import kotlin.reflect.KProperty

/** An option provider that parses the options from command line arguments. */
class CommandLineOptionsProvider(private val parser: ArgParser) : OptionsProvider {
    override fun <T : Any> option(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>
    ): Option<T?> {
        return parser.option(type.toArgType(), name, alias, description).toOption()
    }

    override fun <T : Any> requiredOption(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>,
        default: T?
    ): Option<T> {
        val option = parser.option(type.toArgType(), name, alias, description)
        return if (default == null) option.required().toOption()
        else option.default(default).toOption()
    }

    override fun <T : Any> repeatedOption(
        name: String,
        alias: String?,
        description: String,
        elementType: Class<T>
    ): Option<List<T>> {
        return parser
            .option(elementType.toArgType(), name, alias, description)
            .multiple()
            .toOption()
    }

    fun repositoryRootOption(): Option<File> =
        option<String>()
            .name("repository-root")
            .description("The root directory of the repository.")
            .default(".")
            .transform(::File)
}

private fun <T : Any> SingleNullableOption<T>.toOption(): Option<T?> =
    object : Option<T?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T? =
            this@toOption.provideDelegate(thisRef, property).getValue(thisRef, property)
    }

private fun <T : Any> SingleOption<T, *>.toOption(): Option<T> =
    object : Option<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            this@toOption.provideDelegate(thisRef, property).getValue(thisRef, property)
    }

private fun <T : Any> MultipleOption<T, *, *>.toOption(): Option<List<T>> =
    object : Option<List<T>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> =
            this@toOption.provideDelegate(thisRef, property).getValue(thisRef, property)
    }

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Class<T>.toArgType(): ArgType<T> =
    if (this.isEnum) toArgTypeChoice()
    else when (this.kotlin) {
        Boolean::class -> ArgType.Boolean
        Int::class -> ArgType.Int
        Double::class -> ArgType.Double
        String::class -> ArgType.String
        else -> error("Unsupported option type '$this'!")
    } as ArgType<T>

private fun <T : Any> Class<T>.toArgTypeChoice(): ArgType<T> =
    ArgType.Choice(
        choices = this.enumConstants.asList(),
        toVariant = { stringValue -> this.enumConstants.single { it.toString() == stringValue } }
    )
