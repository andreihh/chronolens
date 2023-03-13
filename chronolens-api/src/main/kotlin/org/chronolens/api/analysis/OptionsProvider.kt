/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.analysis

/**
 * A provider that supplies values for a set of registered [Option]s.
 *
 * Only the following option types are supported:
 * - [String]
 * - [Boolean]
 * - [Int]
 * - [Double]
 * - [Enum]
 * - [List] of the above types
 *
 * Options of other types can be created with [Option.transform] or [Option.transformIfNotNull].
 */
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
    defaultValue: T? = null
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
