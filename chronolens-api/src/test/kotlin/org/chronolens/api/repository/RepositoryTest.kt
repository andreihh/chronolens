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

import kotlin.streams.asStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.chronolens.api.repository.Repository.HistoryProgressListener
import org.chronolens.test.model.revision
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class RepositoryTest {
  @Test
  fun getHistoryStream_delegatesToGetHistory() {
    val revisions = sequenceOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository = mock<Repository> {
      on { getHistory() } doReturn revisions
      on { getHistory(any()) }.thenCallRealMethod()
      on { getHistoryStream(any()) }.thenCallRealMethod()
    }

    assertEquals(expected = revisions.asStream(), actual = repository.getHistoryStream())
  }

  @Test
  fun getHistoryStream_canIterateOnlyOnce() {
    val revisions = sequenceOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository = mock<Repository> {
      on { getHistory() } doReturn revisions
      on { getHistory(any()) }.thenCallRealMethod()
      on { getHistoryStream(any()) }.thenCallRealMethod()
    }

    val history = repository.getHistoryStream()

    history.forEach {}
    assertFails { history.forEach {} }
  }

  @Test
  fun getHistoryStream_withProgressListener_reportsProgress() {
    val revisions = sequenceOf(revision("1") {}, revision("2") {}, revision("3") {})
    val repository = mock<Repository> {
      on { getHistory() } doReturn revisions
      on { getHistory(any()) }.thenCallRealMethod()
      on { getHistoryStream(any()) }.thenCallRealMethod()
    }

    val listener = mock<HistoryProgressListener>()
    repository.getHistoryStream(listener)

    verify(listener).onStart(3)
    revisions.forEach(verify(listener)::onRevision)
    verify(listener).onEnd()
    verifyNoMoreInteractions(listener)
  }
}
