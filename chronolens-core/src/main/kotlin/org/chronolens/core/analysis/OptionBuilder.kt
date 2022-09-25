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
    public fun nullable(): Option<T?> = optionsProvider.option(name, alias, description, type)

    /** Returns a required [Option]. */
    public fun required(): Option<T> =
        optionsProvider.requiredOption(name, alias, description, type)

    /** Returns an [Option] with the specified default [value]. */
    public fun defaultValue(value: T): Option<T> =
        optionsProvider.requiredOption(name, alias, description, type, value)

    /** Returns a repeated [Option]. */
    public fun repeated(): Option<List<T>> =
        optionsProvider.repeatedOption(name, alias, description, type)
}
