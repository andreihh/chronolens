/*
 * Copyright 2018-2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.java

import org.chronolens.api.parsing.SyntaxErrorException
import org.chronolens.model.Identifier
import org.chronolens.model.Signature
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.ArrayType
import org.eclipse.jdt.core.dom.BodyDeclaration
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.NameQualifiedType
import org.eclipse.jdt.core.dom.ParameterizedType
import org.eclipse.jdt.core.dom.PrimitiveType
import org.eclipse.jdt.core.dom.QualifiedType
import org.eclipse.jdt.core.dom.SimpleType
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.Type
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationFragment
import org.eclipse.jdt.core.dom.WildcardType

/**
 * Formats [this] string to a block of code by splitting it into lines, removing blank lines (those
 * which consist only of whitespaces) and trims leading and trailing whitespaces from all lines.
 */
internal fun String?.toBlock(): List<String> =
  orEmpty().lines().filter(String::isNotBlank).map(String::trim)

internal fun <T> Collection<T>.requireDistinct(): Set<T> {
  val unique = LinkedHashSet<T>(size)
  for (element in this) {
    if (element in unique) {
      throw SyntaxErrorException("Duplicated element '$element'!")
    }
    unique += element
  }
  return unique
}

internal fun requireValidIdentifier(identifier: String) {
  if (!Identifier.isValid(identifier)) {
    throw SyntaxErrorException("Invalid identifier '$identifier'!")
  }
}

internal fun requireValidSignature(signature: String) {
  if (!Signature.isValid(signature)) {
    throw SyntaxErrorException("Invalid signature '$signature'!")
  }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> List<*>.requireIsInstance(): List<T> =
  onEach {
    if (it !is T) {
      throw SyntaxErrorException("'$it' is not of type '${T::class}'!")
    }
  }
    as List<T>

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

internal fun MethodDeclaration.returnType(): String? = returnType2?.asString(extraDimensions)

internal fun MethodDeclaration.signature(): String {
  val parameterList =
    parameters().requireIsInstance<SingleVariableDeclaration>().joinToString { parameter ->
      parameter.type() ?: throw AssertionError("'$parameter' must specify type!")
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
  val declarations = mutableListOf<Any?>()
  if (this is EnumDeclaration) {
    declarations.addAll(enumConstants())
  }
  bodyDeclarations().flatMapTo(declarations) { member ->
    if (member is FieldDeclaration) member.fragments() else listOf(member)
  }
  return declarations
}

internal fun AbstractTypeDeclaration.supertypes(): Set<String> =
  when (this) {
      is AnnotationTypeDeclaration -> emptyList<Type>()
      is EnumDeclaration -> superInterfaceTypes()
      is TypeDeclaration -> superInterfaceTypes() + listOfNotNull(superclassType)
      else -> throw AssertionError("Unknown declaration '$this'!")
    }
    .requireIsInstance<Type>()
    .map(Type::asString)
    .requireDistinct()

internal fun AbstractTypeDeclaration.typeModifier(): String =
  when (this) {
    is AnnotationTypeDeclaration -> "@interface"
    is EnumDeclaration -> "enum"
    is TypeDeclaration -> if (isInterface) "interface" else "class"
    else -> throw AssertionError("Unknown declaration '$this'!")
  }

internal fun MethodDeclaration.parameterList(): List<String> =
  parameters().requireIsInstance<SingleVariableDeclaration>().map { it.name() }

internal fun getModifiers(node: ASTNode): List<ASTNode> =
  when (node) {
    is BodyDeclaration -> node.modifiers()
    is SingleVariableDeclaration -> node.modifiers()
    is VariableDeclarationFragment -> getModifiers(node.parent)
    else -> emptyList<ASTNode>()
  }.requireIsInstance()

private fun PrimitiveType.asString(): String =
  when (primitiveTypeCode) {
    PrimitiveType.BOOLEAN -> "boolean"
    PrimitiveType.BYTE -> "byte"
    PrimitiveType.CHAR -> "char"
    PrimitiveType.DOUBLE -> "double"
    PrimitiveType.FLOAT -> "float"
    PrimitiveType.INT -> "int"
    PrimitiveType.LONG -> "long"
    PrimitiveType.SHORT -> "short"
    PrimitiveType.VOID -> "void"
    else -> throw AssertionError("Invalid primitive type '$this'!")
  }

private fun ArrayType.asString(): String = elementType.asString(dimensions)

private fun SimpleType.asString(): String = name.fullyQualifiedName

private fun QualifiedType.asString(): String = "${qualifier.asString()}.${name.fullyQualifiedName}"

private fun NameQualifiedType.asString(): String =
  "${qualifier.fullyQualifiedName}.${name.fullyQualifiedName}"

private fun WildcardType.asString(): String =
  if (bound == null) "?" else "? ${if (isUpperBound) "extends" else "super"} ${bound.asString()}"

private fun ParameterizedType.asString(): String {
  val parameters = typeArguments().requireIsInstance<Type>().map(Type::asString)
  return "${type.asString()}<${parameters.joinToString()}>"
}

private fun Type.asString(): String =
  when (this) {
    is PrimitiveType -> asString()
    is SimpleType -> asString()
    is QualifiedType -> asString()
    is NameQualifiedType -> asString()
    is WildcardType -> asString()
    is ArrayType -> asString()
    is ParameterizedType -> asString()
    else -> throw AssertionError("Unknown type '$this'!")
  }

private fun Type.asString(extraDimensions: Int = 0, isVarargs: Boolean = false): String {
  val suffix = "[]".repeat(extraDimensions) + if (isVarargs) "..." else ""
  return "${asString()}$suffix"
}

private fun ASTNode.baseType(): Type? =
  when (this) {
    is FieldDeclaration -> type
    is AnnotationTypeMemberDeclaration -> type
    is SingleVariableDeclaration -> type
    is VariableDeclarationFragment -> parent.baseType()
    else -> null
  }

internal fun ASTNode.type(): String? =
  when (this) {
    is SingleVariableDeclaration -> baseType()?.asString(extraDimensions, isVarargs)
    is VariableDeclarationFragment -> baseType()?.asString(extraDimensions)
    else -> baseType()?.asString()
  }
