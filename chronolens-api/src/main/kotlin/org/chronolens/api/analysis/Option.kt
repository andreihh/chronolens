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

import kotlin.reflect.KProperty

/**
 * A delegate that provides an option value.
 *
 * Should memoize the option value.
 *
 * @param T the type of the option
 */
public interface Option<T> {
  /** Returns the value of this option. */
  public fun getValue(): T

  /** Delegates to [getValue]. */
  public operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()

  /**
   * Wraps this option delegate and executes the given [validator] on the first option access.
   *
   * The option value will be memoized. If the given [validator] throws an
   * [IllegalArgumentException], it will be wrapped into an [InvalidOptionException]. If the option
   * value is `null`, the [validator] will not be invoked (if only non-null values should be
   * allowed, use required options instead).
   */
  public fun validate(validator: (value: T & Any) -> Unit): Option<T> =
    object : Option<T> {
      private val validatedValue by lazy {
        val value = this@Option.getValue()
        try {
          value?.let { validator(it) }
        } catch (e: IllegalArgumentException) {
          throw InvalidOptionException(e)
        }
        value
      }

      override fun getValue(): T = validatedValue
    }

  /**
   * Wraps this option delegate and executes the given [transform] on the first option access.
   *
   * The option value will be memoized. If the given [transformer] throws an
   * [IllegalArgumentException], it will be wrapped into an [InvalidOptionException].
   */
  public fun <V> transform(transformer: (value: T) -> V): Option<V> =
    object : Option<V> {
      private val transformedValue by lazy {
        val value = this@Option.getValue()
        try {
          transformer(value)
        } catch (e: IllegalArgumentException) {
          throw InvalidOptionException(e)
        }
      }

      override fun getValue(): V = transformedValue
    }

  /**
   * Delegates to [transform], but will not invoke the [transformer] if the option value is `null`
   * and will propagate the `null` value instead.
   */
  public fun <V> transformIfNotNull(transformer: (value: T & Any) -> V): Option<V?> =
    transform { value ->
      value?.let(transformer)
    }

  public companion object {
    /**
     * Delegates to [Option.validate] to ensure the option value is within the ([min], [max]) range.
     *
     * Skips the corresponding check if [min] or [max] are `null`.
     */
    @JvmStatic
    public fun <T : Comparable<T>> Option<T>.constrainTo(
      min: T? = null,
      max: T? = null
    ): Option<T> = validate { value ->
      require(min == null || value >= min) { "Option value can't be less than '$min'!" }
      require(max == null || value <= max) { "Option value can't be greater than '$max'!" }
    }

    /**
     * Throws an [InvalidOptionException] with the given [lazyMessage] if [condition] is `false`.
     */
    @JvmStatic
    public fun requireOption(condition: Boolean, lazyMessage: () -> String) {
      if (!condition) {
        throw InvalidOptionException(lazyMessage())
      }
    }

    /** Throws an [InvalidOptionException] with the given [message]. */
    @JvmStatic
    public fun optionError(message: String): Nothing {
      throw InvalidOptionException(message)
    }
  }
}
