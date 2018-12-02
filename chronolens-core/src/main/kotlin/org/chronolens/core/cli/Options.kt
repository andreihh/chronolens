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

import picocli.CommandLine
import picocli.CommandLine.Help.Visibility.ALWAYS
import picocli.CommandLine.Help.Visibility.NEVER
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.ISetter
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.OptionSpec.Builder
import picocli.CommandLine.ParameterException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class OptionDelegate<out T>(private val option: OptionSpec)
    : ReadOnlyProperty<Command, T> {

    fun getValue(): T = option.getValue()

    override fun getValue(thisRef: Command, property: KProperty<*>): T =
        getValue()
}

abstract class Option<T : Any, B : Option<T, B>>(
    protected val command: Command,
    protected val builder: Builder
) {

    abstract fun self(): B

    fun help(description: String): B {
        builder.description(*description.paragraphs())
        return self()
    }

    fun paramLabel(label: String): B {
        builder.paramLabel(label)
        return self()
    }

    fun validate(block: Validator.(T) -> Unit): B {
        val setter = builder.setter()
        builder.setter(object : ISetter {
            @Suppress("unchecked_cast")
            override fun <R : Any?> set(value: R): R {
                if (value != null) {
                    val cmd = command.command.commandLine()
                    val option = builder.build()
                    Validator(cmd, option, "$value").block(value as T)
                }
                return setter.set(value)
            }
        })
        return self()
    }

    protected fun <T> provideDelegate(
        thisRef: Command,
        propertyName: String?
    ): OptionDelegate<T> {
        val names =
            if (propertyName == null) builder.names()
            else arrayOf(getOptionName(propertyName), *builder.names())
        val parameterLabel = builder.paramLabel()
            ?: names.first().removePrefix("-").removePrefix("-")
        val showDefault = if (builder.defaultValue() != null) ALWAYS else NEVER
        val option = builder
            .names(*names)
            .paramLabel("<$parameterLabel>")
            .showDefaultValue(showDefault)
            .build()
        thisRef.command.addOption(option)
        return OptionDelegate(option)
    }
}

class NullableOption<T : Any>(command: Command, builder: Builder)
    : Option<T, NullableOption<T>>(command, builder) {

    override fun self(): NullableOption<T> = this

    fun provideDelegate(): OptionDelegate<T?> = provideDelegate(command, null)

    operator fun provideDelegate(
        thisRef: Command,
        property: KProperty<*>
    ): OptionDelegate<T?> = provideDelegate(thisRef, property.name)

    @Suppress("unchecked_cast")
    fun <R : Any> type(propertyType: Class<R>): NullableOption<R> {
        builder.type(propertyType)
        return this as NullableOption<R>
    }

    fun required(): RequiredOption<T> =
        RequiredOption(command, builder.required(true))

    fun defaultValue(value: T): RequiredOption<T> =
        RequiredOption(command, builder.defaultValue("$value"))

    fun arity(min: Int? = null, max: Int? = null): RequiredOption<List<T>> {
        val from = "${min ?: 0}"
        val to = if (max == null) "*" else "$max"
        builder
            .auxiliaryTypes(builder.type())
            .type(List::class.java)
            .arity("$from..$to")
        return RequiredOption(command, builder)
    }

    fun arity(range: IntRange): RequiredOption<List<T>> =
        arity(range.start, range.endInclusive)
}

class RequiredOption<T : Any>(command: Command, builder: Builder)
    : Option<T, RequiredOption<T>>(command, builder) {

    override fun self(): RequiredOption<T> = this

    fun provideDelegate(): OptionDelegate<T> = provideDelegate(command, null)

    operator fun provideDelegate(
        thisRef: Command,
        property: KProperty<*>
    ): OptionDelegate<T> = provideDelegate(thisRef, property.name)

    @Suppress("unchecked_cast")
    fun <R : Any> type(propertyType: Class<R>): RequiredOption<R> {
        builder.type(propertyType)
        return this as RequiredOption<R>
    }
}

class Validator(
    private val cmd: CommandLine,
    private val option: OptionSpec,
    private val value: String
) {

    fun require(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) {
            val name = option.longestName()
            val msg = "For option '$name' with value '$value': ${lazyMessage()}"
            throw ParameterException(cmd, msg, option, value)
        }
    }
}

fun <B : Option<Int, B>> Option<Int, B>.restrictTo(
    min: Int? = null,
    max: Int? = null
): B = validate { value ->
    require(min == null || value >= min) { "Value can't be less than $min!" }
    require(max == null || value <= max) { "Value can't be greater than $max!" }
}

fun <B : Option<Double, B>> Option<Double, B>.restrictTo(
    min: Double? = null,
    max: Double? = null
): B = validate { value ->
    require(min == null || value >= min) { "Value can't be less than $min!" }
    require(max == null || value <= max) { "Value can't be greater than $max!" }
}
