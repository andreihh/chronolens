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

package org.chronolens.coupling

import org.chronolens.model.QualifiedSourceNodeId

internal class TemporalContext(
  private val changes: Map<QualifiedSourceNodeId<*>, Int>,
  private val jointChanges: SparseMatrix<QualifiedSourceNodeId<*>, Int>,
  private val temporalCoupling: SparseMatrix<QualifiedSourceNodeId<*>, Double>,
) {
  val ids: Set<QualifiedSourceNodeId<*>>
    get() = changes.keys

  val cells: Collection<Pair<QualifiedSourceNodeId<*>, QualifiedSourceNodeId<*>>> by lazy {
    val c = arrayListOf<Pair<QualifiedSourceNodeId<*>, QualifiedSourceNodeId<*>>>()
    for ((x, row) in jointChanges.entries) {
      for (y in row.keys) {
        c += x to y
      }
    }
    c
  }

  fun revisions(id: QualifiedSourceNodeId<*>): Int = changes[id] ?: 0

  fun revisionsOrNull(id: QualifiedSourceNodeId<*>): Int? = changes[id]

  fun revisions(id1: QualifiedSourceNodeId<*>, id2: QualifiedSourceNodeId<*>): Int =
    jointChanges[id1, id2] ?: 0

  fun revisionsOrNull(id1: QualifiedSourceNodeId<*>, id2: QualifiedSourceNodeId<*>): Int? =
    jointChanges[id1, id2]

  fun coupling(id1: QualifiedSourceNodeId<*>, id2: QualifiedSourceNodeId<*>): Double =
    temporalCoupling[id1, id2] ?: 0.0

  fun couplingOrNull(id1: QualifiedSourceNodeId<*>, id2: QualifiedSourceNodeId<*>): Double? =
    temporalCoupling[id1, id2]

  fun filter(minRevisions: Int, minCoupling: Double): TemporalContext {
    val filteredChanges = changes.filter { (_, revisions) -> revisions >= minRevisions }
    val filteredJointChanges = emptySparseHashMatrix<QualifiedSourceNodeId<*>, Int>()
    val filteredTemporalCoupling = emptySparseHashMatrix<QualifiedSourceNodeId<*>, Double>()
    for ((x, y) in cells) {
      val revisions = revisionsOrNull(x, y) ?: continue
      val coupling = temporalCoupling[x, y] ?: continue
      if (revisions >= minRevisions && coupling >= minCoupling) {
        filteredJointChanges[x, y] = revisions
        filteredTemporalCoupling[x, y] = coupling
      }
    }
    return TemporalContext(
      filteredChanges,
      filteredJointChanges,
      filteredTemporalCoupling,
    )
  }
}

internal typealias SparseMatrix<K, V> = Map<K, Map<K, V>>

internal typealias SparseHashMatrix<K, V> = HashMap<K, HashMap<K, V>>

internal fun <K, V : Any> emptySparseHashMatrix(): SparseHashMatrix<K, V> = hashMapOf()

internal operator fun <K, V : Any> SparseMatrix<K, V>.get(x: K, y: K): V? = this[x].orEmpty()[y]

internal operator fun <K, V : Any> SparseHashMatrix<K, V>.set(
  x: K,
  y: K,
  value: V,
) {
  if (x !in this) {
    this[x] = hashMapOf()
  }
  this.getValue(x)[y] = value
}
