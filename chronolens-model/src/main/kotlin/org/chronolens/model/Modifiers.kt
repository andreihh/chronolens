/*
 * Copyright 2021-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.model

private const val typePrefix = "@type:"
private const val returnTypePrefix = "@return:"

/** Returns the type of [this] variable, or `null` if not specified. */
public val Variable.type: String?
  get() = modifiers.singleOrNull { it.startsWith(typePrefix) }?.removePrefix(typePrefix)

/** Returns the return type of [this] function, or `null` if not specified. */
public val Function.returnType: String?
  get() = modifiers.singleOrNull { it.startsWith(returnTypePrefix) }?.removePrefix(returnTypePrefix)

/** Returns the modifier corresponding to the given [type]. */
public fun typeModifierOf(type: String): String = "$typePrefix$type"

/** Returns the modifier corresponding to the given [returnType]. */
public fun returnTypeModifierOf(returnType: String): String = "$returnTypePrefix$returnType"
