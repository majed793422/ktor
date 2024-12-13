/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Represents a test case with associated data and retry attempt information.
 *
 * @property data The input data for the test case.
 * @property retry The current retry attempt number for this test case. `0` means the initial test run before retries.
 */
data class TestCase<T>(val data: T, val retry: Int)

/**
 * Represents a failure that occurred during the execution of a test case, along with the associated test data.
 *
 * @param cause The exception that caused the test to fail.
 * @param data The data associated with the test case that failed.
 */
data class TestFailure<T>(val cause: Throwable, val data: T)

/**
 * Executes multiple test cases with retry capabilities and timeout control.
 * Timeout is independent for each attempt in each test case.
 *
 * Example usage:
 * ```
 * @Test
 * fun dataDrivenTest() = runTestsWithData(
 *     testCases = listOf("test1", "test2"),
 *     timeout = 10.seconds,
 *     retries = 2,
 *     catch = { (cause, data) -> println("Test $data failed: $cause") }
 * ) { (data, attempt) ->
 *     // test implementation
 * }.then { println("Final steps go here") }
 * ```
 *
 * @param testCases Data to be used in tests. Each element represents a separate test case.
 * @param context Optional coroutine context for test execution. Defaults to [EmptyCoroutineContext].
 * @param timeout Maximum duration allowed for each test attempt. Defaults to 1 minute.
 * @param retries Number of additional attempts after initial failure (0 means no retries).
 * @param catch Handler for test failures. Called once per failed test case with the last failure cause.
 *  Note that it is the responsibility of calling code to throw the final aggregated exception.
 * @param test Test execution block. Receives [TestCase] containing both test data and current attempt number.
 *
 * @return [TestResult] representing the completion of all test cases.
 */
fun <T> runTestsWithData(
    testCases: Iterable<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 1.minutes,
    retries: Int = 1,
    catch: (TestFailure<T>) -> Unit = { throw it.cause },
    test: suspend TestScope.(TestCase<T>) -> Unit,
): TestResult {
    check(retries >= 0) { "Retries count shouldn't be negative but it is $retries" }

    return runTestForEach(testCases) { data ->
        testWithRecover(recover = { catch(TestFailure(it, data)) }) {
            retryTest(retries) { retry ->
                runTest(context, timeout = timeout) { test(TestCase(data, retry)) }
            }
        }
    }
}

/**
 * Executes the provided [block] after the test.
 * It is the only way to execute something **after** test on JS/WasmJS targets.
 * @see TestResult
 */
expect fun TestResult.then(block: () -> Unit): TestResult

internal expect inline fun testWithRecover(noinline recover: (Throwable) -> Unit, test: () -> TestResult): TestResult

internal expect inline fun <T> runTestForEach(items: Iterable<T>, test: (T) -> TestResult): TestResult
internal expect fun retryTest(retries: Int, test: (Int) -> TestResult): TestResult
