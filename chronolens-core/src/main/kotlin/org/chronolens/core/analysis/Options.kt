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

import kotlin.reflect.KProperty

/** A provider that supplies values for a set of registered [Option]s. */
public interface OptionsProvider {
    /**
     * Returns a fluent [OptionBuilder] that will register the created option to this provider.
     *
     * The [OptionBuilder.type] must be set on the returned builder.
     */
    public fun <T : Any> untypedOption(): OptionBuilder<T> = OptionBuilder(this)

    /** Registers a nullable [Option] with the given parameters. */
    public fun <T : Any> option(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>
    ): Option<T?>

    /** Registers a required [Option] with the given parameters. */
    public fun <T : Any> requiredOption(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>,
        default: T? = null
    ): Option<T>

    /** Registers a repeated [Option] with the given parameters. */
    public fun <T : Any> repeatedOption(
        name: String,
        alias: String?,
        description: String,
        elementType: Class<T>
    ): Option<List<T>>
}

/**
 * Returns a fluent [OptionBuilder] that will register the created option to this provider.
 *
 * The returned builder will already have the [OptionBuilder.type] set to [T].
 */
public inline fun <reified T : Any> OptionsProvider.option(): OptionBuilder<T> =
    untypedOption<T>().type(T::class.java)

/**
 * A delegate that provides an option value.
 *
 * Should memoize the option value.
 *
 * @param T the type of the option
 */
public interface Option<T> {
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T

    /**
     * Wraps this option delegate and executes the given [validator] on the first option access.
     *
     * The option value will be memoized. If the given [validator] throws an
     * [IllegalArgumentException], it will be wrapped into an [InvalidOptionException]. If the
     * option value is `null`, the [validator] will not be invoked (if only non-null values should
     * be allowed, use required options instead).
     */
    public fun validate(validator: (value: T & Any) -> Unit): Option<T> =
        object : Option<T> {
            private lateinit var validatedOption: Lazy<T>

            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                if (!this::validatedOption.isInitialized) {
                    validatedOption = lazy {
                        val value = this@Option.getValue(thisRef, property)
                        try {
                            value?.let { validator(it) }
                        } catch (e: IllegalArgumentException) {
                            throw InvalidOptionException(e)
                        }
                        value
                    }
                }
                return validatedOption.getValue(thisRef, property)
            }
        }

    /**
     * Wraps this option delegate and executes the given [transform] on the first option access.
     *
     * The option value will be memoized. If the given [transformer] throws an
     * [IllegalArgumentException], it will be wrapped into an [InvalidOptionException].
     */
    public fun <V> transform(transformer: (value: T) -> V): Option<V> =
        object : Option<V> {
            private lateinit var transformedOption: Lazy<V>

            override fun getValue(thisRef: Any?, property: KProperty<*>): V {
                if (!this::transformedOption.isInitialized) {
                    transformedOption = lazy {
                        val value = this@Option.getValue(thisRef, property)
                        try {
                            transformer(value)
                        } catch (e: IllegalArgumentException) {
                            throw InvalidOptionException(e)
                        }
                    }
                }
                return transformedOption.getValue(thisRef, property)
            }
        }

    /**
     * Delegates to [transform], but will not invoke the [transformer] if the option value is `null`
     * and will propagate the `null` value instead.
     */
    public fun <V> transformIfNotNull(transformer: (value: T & Any) -> V): Option<V?> =
        transform { it?.let(transformer) }
}

/**
 * A fluent [Option] builder that will register the created option to the given [optionsProvider].
 */
public class OptionBuilder<T : Any>(private val optionsProvider: OptionsProvider) {
    private lateinit var name: String
    private var alias: String? = null
    private lateinit var description: String
    private lateinit var type: Class<T>

    /** Sets the name of this option. Required. */
    public fun name(name: String): OptionBuilder<T> {
        this.name = name
        return this
    }

    /** Sets the short name of this option. Optional. */
    public fun alias(alias: String): OptionBuilder<T> {
        this.alias = alias
        return this
    }

    /** Sets the description of this option. Required. */
    public fun description(description: String): OptionBuilder<T> {
        this.description = description
        return this
    }

    /** Sets the value type of this option. Required. */
    public fun type(type: Class<T>): OptionBuilder<T> {
        this.type = type
        return this
    }

    /** Returns a nullable [Option]. */
    public fun nullable(): Option<T?> =
        optionsProvider.option(name, alias, description, type)

    /** Returns a required [Option]. */
    public fun required(): Option<T> =
        optionsProvider.requiredOption(name, alias, description, type)

    /** Returns an [Option] with the specified default [value]. */
    public fun default(value: T): Option<T> =
        optionsProvider.requiredOption(name, alias, description, type, value)

    /** Returns a repeated [Option]. */
    public fun repeated(): Option<List<T>> =
        optionsProvider.repeatedOption(name, alias, description, type)
}

/** Signals that an invalid option has been encountered. */
public class InvalidOptionException : IllegalArgumentException {
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String) : super(message)
}

/** Throws an [InvalidOptionException] with the given [lazyMessage] if [condition] is `false`. */
public fun requireOption(condition: Boolean, lazyMessage: () -> String) {
    if (!condition) {
        throw InvalidOptionException(lazyMessage())
    }
}

/** Throws an [InvalidOptionException] with the given [message]. */
public fun optionError(message: String): Nothing {
    throw InvalidOptionException(message)
}
