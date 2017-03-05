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

package org.chronos.core.delta

/**
 * A series of changes which should be applied to an object.
 *
 * @param T the type of the changed object
 */
interface Transaction<T> {
    companion object {
        /**
         * Applies the given `transaction` on this object, or does nothing if
         * `transaction` is `null`.
         */
        fun <T> T.apply(transaction: Transaction<T>?): T =
                if (transaction != null) transaction.applyOn(this) else this

        /** Applies the given `transactions` on this object. */
        fun <T> T.apply(transactions: List<Transaction<T>>): T =
                transactions.fold(this) { subject, change ->
                    subject.apply(change)
                }

        fun <T> T.apply(
                firstTransaction: Transaction<T>,
                secondTransaction: Transaction<T>,
                vararg remainingTransactions: Transaction<T>
        ): T = apply(listOf(
                firstTransaction,
                secondTransaction,
                *remainingTransactions
        ))
    }

    /**
     * Applies this transaction on the given `subject`.
     *
     * @param subject the object which should be changed
     * @return the object resulting from the applied transaction
     * @throws IllegalStateException if the object has an invalid state and this
     * transaction couldn't be applied
     */
    fun applyOn(subject: T): T
}
