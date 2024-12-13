/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.test.TestResult
import kotlin.js.Promise

actual fun TestResult.then(block: () -> Unit): TestResult = asPromise().then { block() }.asTestResult()

internal actual fun combine(results: Array<TestResult>): TestResult {
    @Suppress("UNCHECKED_CAST")
    return Promise.all(results as Array<Promise<*>>).asTestResult()
}

internal actual fun TestResult.catch(action: (Throwable) -> Any): TestResult {
    return this.asPromise().then(onFulfilled = null, onRejected = action).asTestResult()
}

@Suppress("CAST_NEVER_SUCCEEDS")
private fun TestResult.asPromise(): Promise<Unit> = this as Promise<Unit>

@Suppress("CAST_NEVER_SUCCEEDS")
private fun Promise<*>.asTestResult(): TestResult = this as TestResult
