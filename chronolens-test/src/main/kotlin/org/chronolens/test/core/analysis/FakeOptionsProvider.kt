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

package org.chronolens.test.core.analysis

import org.chronolens.core.analysis.Option
import org.chronolens.core.analysis.OptionsProvider

internal class FakeOptionsProvider(private val options: Map<String, Any>) : OptionsProvider {
    private fun getOptionValue(name: String, alias: String?): Any? {
        val nameValue = options[name]
        val aliasValue = alias?.let(options::get)
        require(nameValue == null || aliasValue == null) {
            "Option '$name' with value '$nameValue' has alias '$alias' with value '$aliasValue'!"
        }
        return nameValue ?: aliasValue
    }

    override fun <T : Any> option(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>
    ): Option<T?> {
        val value = getOptionValue(name, alias) ?: return OptionHolder(null)
        require(type.isInstance(value)) {
            "Option '$name' with value '$value' is not of type '$type'!"
        }
        return OptionHolder(type.cast(value))
    }

    override fun <T : Any> requiredOption(
        name: String,
        alias: String?,
        description: String,
        type: Class<T>,
        defaultValue: T?
    ): Option<T> {
        val value = getOptionValue(name, alias) ?: defaultValue
        requireNotNull(value) { "Option '$name' is required!" }
        require(type.isInstance(value)) {
            "Option '$name' with value '$value' is not of type '$type'!"
        }
        return OptionHolder(type.cast(value))
    }

    override fun <T : Any> repeatedOption(
        name: String,
        alias: String?,
        description: String,
        elementType: Class<T>
    ): Option<List<T>> {
        val value = getOptionValue(name, alias) ?: return OptionHolder(emptyList())
        require(value is List<*>) { "Repeated option '$name' with value '$value' is not a list!" }
        val typedValue = mutableListOf<T>()
        for (element in value) {
            require(elementType.isInstance(element)) {
                "Repeated option '$name' with element '$element' is not of type '$elementType'!"
            }
            typedValue += elementType.cast(element)
        }
        return OptionHolder(typedValue)
    }
}
