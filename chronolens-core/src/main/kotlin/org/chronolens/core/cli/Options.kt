/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

@file:JvmName("Options")

package org.chronolens.core.cli

import picocli.CommandLine.Help.Visibility.ALWAYS
import picocli.CommandLine.Help.Visibility.NEVER
import picocli.CommandLine.Model.IGetter
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.OptionSpec.Builder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class OptionDelegate<out T>(private val option: OptionSpec)
    : ReadOnlyProperty<Subcommand, T> {

    override fun getValue(thisRef: Subcommand, property: KProperty<*>): T =
        option.getValue()
}

open class NullableOption<out T : Any>(internal val builder: Builder) {
    open operator fun provideDelegate(
        thisRef: Subcommand,
        property: KProperty<*>
    ): OptionDelegate<T?> {
        val name = getOptionName(property.name)
        val parameterLabel = builder.paramLabel() ?: name.removePrefix("--")
        val showDefault = if (builder.defaultValue() != null) ALWAYS else NEVER
        val names = builder.names()
        val option = builder.names(name, *names)
            .paramLabel("<$parameterLabel>")
            .showDefaultValue(showDefault)
            .build()
        thisRef.command.addOption(option)
        return OptionDelegate(option)
    }
}

class Option<out T : Any>(builder: Builder) : NullableOption<T>(builder) {
    @Suppress("unchecked_cast")
    override operator fun provideDelegate(
        thisRef: Subcommand,
        property: KProperty<*>
    ): OptionDelegate<T> =
        super.provideDelegate(thisRef, property) as OptionDelegate<T>
}

private fun String.getWords(): List<String> {
    val words = arrayListOf<String>()
    var word = StringBuilder()
    for (char in this.capitalize()) {
        if (char.isUpperCase()) {
            if (!word.isBlank()) {
                words += word.toString()
                word = StringBuilder()
            }
            word.append(char.toLowerCase())
        } else {
            word.append(char)
        }
    }
    if (word.isNotBlank()) {
        words += word.toString()
    }
    return words
}

private fun getOptionName(propertyName: String): String =
    propertyName.getWords().joinToString(separator = "-", prefix = "--")

internal fun <T : Command> getCommandName(type: KClass<T>): String {
    val className = type.simpleName ?: error("")
    return className.removeSuffix("Command")
        .getWords()
        .joinToString(separator = "-")
}

fun <T : Any, Opt : NullableOption<T>> Opt.help(description: String): Opt {
    builder.description(description.paragraph())
    return this
}

fun <T : Any, Opt : NullableOption<T>> Opt.paramLabel(label: String): Opt {
    builder.paramLabel(label)
    return this
}

fun <T : Any> NullableOption<T>.required(): Option<T> {
    builder.required(true)
    return Option(builder)
}

fun <T : Any> NullableOption<T>.default(value: T): Option<T> {
    builder.defaultValue("$value")
    return Option(builder)
}

fun <Opt : NullableOption<Int>> Opt.restrictTo(
    min: Int? = null,
    max: Int? = null
): Opt = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}

fun <Opt : NullableOption<Double>> Opt.restrictTo(
    min: Double? = null,
    max: Double? = null
): Opt = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}

class Validator(val name: String) {
    inline fun require(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) exit(lazyMessage())
    }
}

fun <T : Any, Opt : NullableOption<T>> Opt.validate(
    block: Validator.(T) -> Unit
): Opt {
    val getter = builder.getter()
    builder.getter(object : IGetter {
        @Suppress("unchecked_cast")
        override fun <R> get(): R {
            val value = getter.get<T>() ?: return null as R
            val name = builder.names().first()
            Validator(name).block(value)
            return value as R
        }
    })
    return this
}
