/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.test

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RunTestsWithDataTest {

    @Test
    fun testBasicSuccess() = runTestsWithData(
        singleTestCase,
        test = { /* simple successful operation */ },
        catch = { fail("Test should succeed") }
    )

    @Test
    fun testMultipleRetriesUntilSuccess(): TestResult {
        var successfulAttempt = 0
        return runTestsWithData(
            singleTestCase,
            retries = 3,
            test = { (_, attempt) ->
                if (attempt < 3) fail("Attempt $attempt")
                successfulAttempt = attempt
            },
            catch = { fail("Test should succeed on third attempt") }
        ).then { assertEquals(3, successfulAttempt) }
    }

    @Test
    fun testZeroRetries(): TestResult {
        var failureCount = 0
        return runTestsWithData(
            singleTestCase,
            retries = 0,
            test = { fail("Should fail") },
            catch = { failureCount++ }
        ).then { assertEquals(1, failureCount) }
    }

    @Test
    fun testExhaustedRetries(): TestResult {
        var failureCount = 0
        return runTestsWithData(
            singleTestCase,
            retries = 2,
            test = { fail("Always fail") },
            catch = { failureCount++ }
        ).then { assertEquals(1, failureCount) }
    }

    @Test
    fun testMultipleItemsExecution(): TestResult {
        val executedItems = mutableSetOf<Int>()
        return runTestsWithData(
            testCases = 1..3,
            test = { (item, _) -> executedItems.add(item) },
            catch = { fail("Test should succeed") }
        ).then { assertEquals(setOf(1, 2, 3), executedItems) }
    }

    @Test
    fun testEmptyItemsList() = runTestsWithData(
        testCases = emptyList<Int>(),
        test = { fail("Should not be called") },
        catch = { fail("Should not be called") }
    )

    @Test
    fun testContextPropagation() = runTestsWithData(
        singleTestCase,
        context = CoroutineName("TestContext"),
        test = { assertEquals("TestContext", currentCoroutineContext()[CoroutineName]?.name) },
        catch = { fail("Test should succeed") }
    )

    @Test
    fun testFailByTimeout(): TestResult {
        var failureCount = 0
        return runTestsWithData(
            singleTestCase,
            timeout = 10.milliseconds,
            test = { realTimeDelay(1.seconds) },
            catch = { failureCount++ },
        ).then { assertEquals(1, failureCount) }
    }

    @Test
    fun testRetriesHaveIndependentTimeout() = runTestsWithData(
        singleTestCase,
        retries = 1,
        timeout = 15.milliseconds,
        test = { (_, attempt) ->
            realTimeDelay(10.milliseconds)
            if (attempt == 0) fail("Try again, please")
        },
        catch = { fail("Test should succeed") },
    )

    @Test
    fun testDifferentItemsHaveIndependentTimeout() = runTestsWithData(
        testCases = 1..2,
        timeout = 15.milliseconds,
        test = { realTimeDelay(10.milliseconds) },
        catch = { fail("Test should succeed") },
    )
}

private val singleTestCase = listOf(1)

private suspend fun realTimeDelay(duration: Duration) {
    withContext(Dispatchers.Default.limitedParallelism(1)) { delay(duration) }
}
