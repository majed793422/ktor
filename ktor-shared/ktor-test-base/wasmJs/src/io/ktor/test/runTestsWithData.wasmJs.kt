/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.test.TestResult
import kotlin.js.Promise

actual fun TestResult.then(block: () -> Unit): TestResult = asPromise().then { block().toJsReference() }.asTestResult()

internal actual fun combine(results: Array<TestResult>): TestResult {
    @Suppress("UNCHECKED_CAST")
    return Promise.all((results as Array<Promise<*>>).toJsArray()).asTestResult()
}

internal actual fun TestResult.catch(action: (Throwable) -> Any): TestResult {
    return this.asPromise()
        .then(onFulfilled = null, onRejected = { action(it.toThrowableOrNull()!!).toJsReference() })
        .asTestResult()
}

@Suppress("CAST_NEVER_SUCCEEDS")
private fun TestResult.asPromise(): Promise<JsAny> = this as Promise<JsAny>

@Suppress("CAST_NEVER_SUCCEEDS")
private fun Promise<*>.asTestResult(): TestResult = this as TestResult

private fun <T : JsAny?> Array<T>.toJsArray(): JsArray<T> {
    val destination = JsArray<T>()
    for (i in this.indices) {
        destination[i] = this[i]
    }
    return destination
}
