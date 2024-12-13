/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.test.TestResult

actual fun TestResult.then(block: () -> Unit): TestResult = apply { block() }

internal actual inline fun testWithRecover(recover: (Throwable) -> Unit, test: () -> TestResult): TestResult {
    try {
        test()
    } catch (cause: Throwable) {
        recover(cause)
    }
    @Suppress("CAST_NEVER_SUCCEEDS")
    return Unit as TestResult
}

internal actual inline fun <T> runTestForEach(items: Iterable<T>, test: (T) -> TestResult): TestResult {
    for (item in items) test(item)
    @Suppress("CAST_NEVER_SUCCEEDS")
    return Unit as TestResult
}

internal actual inline fun retryTest(retries: Int, test: (Int) -> TestResult): TestResult {
    lateinit var lastCause: Throwable
    repeat(retries + 1) { attempt ->
        try {
            return test(attempt)
        } catch (cause: Throwable) {
            lastCause = cause
        }
    }
    throw lastCause
}
