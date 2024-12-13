/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.test.TestResult

internal actual inline fun testWithRecover(
    noinline recover: (Throwable) -> Unit,
    test: () -> TestResult
): TestResult = test().catch(recover)

internal actual inline fun <T> runTestForEach(items: Iterable<T>, test: (T) -> TestResult): TestResult {
    return combine(items.map(test).toTypedArray())
}

internal actual fun retryTest(retries: Int, test: (Int) -> TestResult): TestResult {
    var result = test(0)
    for (retryNumber in 1..retries) result = result.catch { test(retryNumber) }
    return result
}

internal expect fun combine(results: Array<TestResult>): TestResult

internal expect fun TestResult.catch(action: (Throwable) -> Any): TestResult
