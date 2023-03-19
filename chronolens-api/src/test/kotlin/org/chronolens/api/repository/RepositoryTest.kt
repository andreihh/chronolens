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

package org.chronolens.api.repository

import kotlin.streams.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.chronolens.api.repository.Repository.HistoryProgressListener
import org.chronolens.model.Revision
import org.chronolens.test.model.revision
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class RepositoryTest {
  @Test
  fun getHistoryStream_delegatesToGetHistory() {
    val revisions = listOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository =
      mock<Repository> {
        on { getHistory() } doReturn revisions.asSequence()
        on { getHistory(anyOrNull()) }.thenCallRealMethod()
        on { getHistoryStream(anyOrNull()) }.thenCallRealMethod()
      }

    val history = repository.getHistoryStream()

    assertEquals(expected = revisions, actual = history.toList())
  }

  @Test
  fun getHistoryStream_canIterateOnlyOnce() {
    val revisions = sequenceOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository =
      mock<Repository> {
        on { getHistory() } doReturn revisions
        on { getHistory(anyOrNull()) }.thenCallRealMethod()
        on { getHistoryStream(anyOrNull()) }.thenCallRealMethod()
      }

    val history = repository.getHistoryStream()

    history.toList()
    assertFails { history.toList() }
  }

  @Test
  fun getHistoryStream_withProgressListener_isLazy() {
    val revisions = listOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository =
      mock<Repository> {
        on { listRevisions() } doReturn revisions.map(Revision::id)
        on { getHistory() } doReturn revisions.asSequence()
        on { getHistory(anyOrNull()) }.thenCallRealMethod()
        on { getHistoryStream(anyOrNull()) }.thenCallRealMethod()
      }
    val listener = mock<HistoryProgressListener>()

    repository.getHistoryStream(listener)

    verifyNoMoreInteractions(listener)
  }

  @Test
  fun getHistoryStream_withProgressListener_reportsProgress() {
    val revisions = listOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository =
      mock<Repository> {
        on { listRevisions() } doReturn revisions.map(Revision::id)
        on { getHistory() } doReturn revisions.asSequence()
        on { getHistory(anyOrNull()) }.thenCallRealMethod()
        on { getHistoryStream(anyOrNull()) }.thenCallRealMethod()
      }
    val listener = mock<HistoryProgressListener>()

    repository.getHistoryStream(listener).toList()

    verify(listener).onStart(3)
    revisions.forEach { verify(listener).onRevision(it) }
    verify(listener).onEnd()
    verifyNoMoreInteractions(listener)
  }
}
