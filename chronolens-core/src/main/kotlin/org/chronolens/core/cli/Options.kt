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
import picocli.CommandLine.Model.ISetter
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.OptionSpec.Builder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class OptionDelegate<out T>(private val option: OptionSpec)
    : ReadOnlyProperty<Command, T> {

    fun getValue(): T = option.getValue()

    override fun getValue(thisRef: Command, property: KProperty<*>): T =
        getValue()
}

class NullableOption<T : Any>(private val builder: Builder) {
    fun provideDelegate(
        thisRef: Command,
        propertyName: String
    ): OptionDelegate<T?> = provideDelegate(builder, thisRef, propertyName)

    operator fun provideDelegate(
        thisRef: Command,
        property: KProperty<*>
    ): OptionDelegate<T?> = provideDelegate(thisRef, property.name)

    fun help(description: String): NullableOption<T> {
        builder.description(*description.paragraphs())
        return this
    }

    fun paramLabel(label: String): NullableOption<T> {
        builder.paramLabel(label)
        return this
    }

    fun required(): Option<T> = Option(builder.required(true))

    fun defaultValue(value: T): Option<T> =
        Option(builder.defaultValue("$value"))

    fun validate(block: Validator.(T) -> Unit): NullableOption<T> {
        validate(builder, block)
        return this
    }
}

class Option<out T : Any>(private val builder: Builder) {
    fun provideDelegate(
        thisRef: Command,
        propertyName: String
    ): OptionDelegate<T> = provideDelegate(builder, thisRef, propertyName)

    operator fun provideDelegate(
        thisRef: Command,
        property: KProperty<*>
    ): OptionDelegate<T> = provideDelegate(thisRef, property.name)

    fun validate(block: Validator.(T) -> Unit): Option<T> {
        validate(builder, block)
        return this
    }
}

class Validator(val name: String) {
    inline fun require(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) exit(lazyMessage())
    }
}

private fun <T> provideDelegate(
    builder: Builder,
    thisRef: Command,
    propertyName: String
): OptionDelegate<T> {
    val name = getOptionName(propertyName)
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

private fun getOptionName(propertyName: String): String =
    propertyName.words().joinToString(separator = "-", prefix = "--")

private fun <T : Any> validate(builder: Builder, block: Validator.(T) -> Unit) {
    val setter = builder.setter()
    builder.setter(object : ISetter {
        @Suppress("unchecked_cast")
        override fun <R : Any?> set(value: R): R {
            if (value != null) {
                val name = builder.names().first()
                Validator(name).block(value as T)
            }
            return setter.set(value)
        }
    })
}

fun NullableOption<Int>.restrictTo(
    min: Int? = null,
    max: Int? = null
): NullableOption<Int> = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}

fun Option<Int>.restrictTo(
    min: Int? = null,
    max: Int? = null
): Option<Int> = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}

fun NullableOption<Double>.restrictTo(
    min: Double? = null,
    max: Double? = null
): NullableOption<Double> = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}

fun Option<Double>.restrictTo(
    min: Double? = null,
    max: Double? = null
): Option<Double> = validate { value ->
    require(min == null || value >= min) { "$name can't be less than $min!" }
    require(max == null || value <= max) { "$name can't be greater than $max!" }
}
