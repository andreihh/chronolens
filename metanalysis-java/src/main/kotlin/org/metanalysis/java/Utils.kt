/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.java

import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.BodyDeclaration
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.Type
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationFragment
import org.metanalysis.core.parsing.SyntaxErrorException

/**
 * Formats [this] string to a block of code by splitting it into lines, removing
 * blank lines (those which consist only of whitespaces) and trims leading and
 * trailing whitespaces from all lines.
 */
internal fun String?.toBlock(): List<String> =
    orEmpty().lines().filter(String::isNotBlank).map(String::trim)

internal fun <T> Collection<T>.requireDistinct(): Set<T> {
    val unique = linkedSetOf<T>()
    for (element in this) {
        if (element in unique) {
            throw SyntaxErrorException("Duplicated element '$element'!")
        }
        unique += element
    }
    return unique
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> List<*>.requireIsInstance(): List<T> = onEach {
    if (it !is T) {
        throw SyntaxErrorException("'$it' is not of type '${T::class}'!")
    }
} as List<T>

internal fun requireNotMalformed(node: ASTNode) {
    if ((node.flags and (ASTNode.MALFORMED or ASTNode.RECOVERED)) != 0) {
        throw SyntaxErrorException("Malformed AST node!")
    }
}

/** Returns the name of this node. */
internal fun AbstractTypeDeclaration.name(): String = name.identifier
internal fun AnnotationTypeMemberDeclaration.name(): String = name.identifier
internal fun EnumConstantDeclaration.name(): String = name.identifier
internal fun VariableDeclaration.name(): String = name.identifier

private fun Type?.asString(
    extraDimensions: Int = 0,
    isVarargs: Boolean = false
): String? {
    val suffix = "[]".repeat(extraDimensions) + if (isVarargs) "..." else ""
    return if (this == null) null else "$this$suffix" // TODO: stringify type
}

private fun getBaseType(node: ASTNode): Type? = when (node) {
    is FieldDeclaration -> node.type
    is AnnotationTypeMemberDeclaration -> node.type
    is SingleVariableDeclaration -> node.type
    is VariableDeclarationFragment -> getBaseType(node.parent)
    else -> null
}

internal fun ASTNode.type(): String? = when (this) {
    is SingleVariableDeclaration ->
        getBaseType(this).asString(extraDimensions, isVarargs)
    is VariableDeclarationFragment ->
        getBaseType(this).asString(extraDimensions)
    else -> getBaseType(this).asString()
}

internal fun MethodDeclaration.returnType(): String? =
    returnType2.asString(extraDimensions)

internal fun MethodDeclaration.signature(): String {
    val parameterList = getParameters(this).joinToString { parameter ->
        parameter.type()
            ?: throw AssertionError("'$parameter' must have specified type!")
    }
    return "${name.identifier}($parameterList)"
}

/**
 * Returns the list of all members of this type.
 *
 * The returned elements can be of the following types:
 * - [AbstractTypeDeclaration]
 * - [AnnotationTypeMemberDeclaration]
 * - [EnumConstantDeclaration]
 * - [VariableDeclaration]
 * - [MethodDeclaration]
 * - [Initializer]
 */
internal fun AbstractTypeDeclaration.members(): List<*> {
    val declarations = arrayListOf<Any?>()
    if (this is EnumDeclaration) {
        declarations.addAll(enumConstants())
    }
    bodyDeclarations().flatMapTo(declarations) { member ->
        if (member is FieldDeclaration) member.fragments()
        else listOf(member)
    }
    return declarations
}

internal fun AbstractTypeDeclaration.supertypes(): List<Type> = when (this) {
    is AnnotationTypeDeclaration -> emptyList()
    is EnumDeclaration -> superInterfaceTypes()
    is TypeDeclaration -> superInterfaceTypes() + superclassType
    else -> throw AssertionError("Unknown declaration '$this'!")
}.requireIsInstance<Type?>().filterNotNull()

internal fun AbstractTypeDeclaration.typeModifier(): String = when (this) {
    is AnnotationTypeDeclaration -> "@interface"
    is EnumDeclaration -> "enum"
    is TypeDeclaration -> if (isInterface) "interface" else "class"
    else -> throw AssertionError("Unknown declaration '$this'!")
}

internal fun getParameters(
    method: MethodDeclaration
): List<SingleVariableDeclaration> = method.parameters().requireIsInstance()

internal fun getModifiers(node: ASTNode): List<ASTNode> = when (node) {
    is BodyDeclaration -> node.modifiers()
    is SingleVariableDeclaration -> node.modifiers()
    is VariableDeclarationFragment -> getModifiers(node.parent)
    else -> emptyList()
}.requireIsInstance()
