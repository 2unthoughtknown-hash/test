package com.nec.highfuncmiddle.repository

import android.util.Log
import com.nec.highfuncmiddle.data.api.ApiErrorType
import com.nec.highfuncmiddle.data.api.ApiResult
import com.nec.highfuncmiddle.data.api.DeliverFileApplyResultApi
import com.nec.highfuncmiddle.data.msgw.DeliverFileApplyResult
import com.nec.highfuncmiddle.domain.UseCaseResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeliverFileApplyResultRepositoryTest {
    private lateinit var deliverFileApplyResultRepository: DeliverFileApplyResultRepository
    private lateinit var deliverFileApplyResultApi: DeliverFileApplyResultApi

    private val status = "1000"
    private val errorReason = "Test Error Reason"
    private val version = DeliverFileApplyResult.Version(
        iM28MGOS = "1.0.0",
        iM28MGBL = "1.1.0",
        iM28MGAP = "2.0.0",
        iM28MMAP = "1.5.0",
        iM28SBOS = "1.2.0",
        iM28SBBL = "1.3.0",
        iM28SBAP = "2.1.0"
    )
    private val reserve = DeliverFileApplyResult.Reserve(
        iM28MGOS = listOf("1.0.1", "1.0.2"),
        iM28MGBL = listOf("1.1.1"),
        iM28MGAP = listOf("2.0.1", "2.0.2"),
        iM28MMAP = listOf("1.5.1"),
        iM28SBOS = listOf("1.2.1"),
        iM28SBBL = listOf("1.3.1"),
        iM28SBAP = listOf("2.1.1")
    )
    private val json = DeliverFileApplyResult.Response(status)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0

        mockkStatic(DeliverFileApplyResultRepository::class)

        deliverFileApplyResultApi = mockk<DeliverFileApplyResultApi>()
        deliverFileApplyResultRepository = DeliverFileApplyResultRepository(deliverFileApplyResultApi)
    }

    @After
    fun tearDown() = runTest {
        unmockkAll()
    }

    @Test
    fun deliverFileApplyResultTest() = runTest {
        val saveError: suspend (ApiErrorType, String) -> Pair<String, String> = mockk()

        // 正常ルート（全パラメータあり）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Success(json)
        var result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve, saveError)
        Assert.assertTrue(result is UseCaseResult.Success)
        val successResult = result as UseCaseResult.Success
        Assert.assertEquals(status, successResult.data)

        // 正常ルート（null パラメータ）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Success(json)
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, null, version, null, saveError)
        Assert.assertTrue(result is UseCaseResult.Success)
        val successResult2 = result as UseCaseResult.Success
        Assert.assertEquals(status, successResult2.data)

        // 異常ルート（CONNECTION_FAILED）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.CONNECTION_FAILED, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR01" to "Connection failed" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult1 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR01", failureResult1.errorInfo.first)
        Assert.assertEquals("Connection failed", failureResult1.errorInfo.second)

        // 異常ルート（TIMEOUT）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.TIMEOUT, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR02" to "Timeout error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult2 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR02", failureResult2.errorInfo.first)
        Assert.assertEquals("Timeout error", failureResult2.errorInfo.second)

        // 異常ルート（SERVER_ERROR）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.SERVER_ERROR, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR03" to "Server error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult3 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR03", failureResult3.errorInfo.first)
        Assert.assertEquals("Server error", failureResult3.errorInfo.second)

        // 異常ルート（PARSE_ERROR）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.PARSE_ERROR, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR04" to "Parse error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult4 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR04", failureResult4.errorInfo.first)
        Assert.assertEquals("Parse error", failureResult4.errorInfo.second)

        // 異常ルート（NETWORK_UNAVAILABLE）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.NETWORK_UNAVAILABLE, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR05" to "Network unavailable" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult5 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR05", failureResult5.errorInfo.first)
        Assert.assertEquals("Network unavailable", failureResult5.errorInfo.second)

        // 異常ルート（RESPONSE_ERROR）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.RESPONSE_ERROR, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR06" to "Response error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult6 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR06", failureResult6.errorInfo.first)
        Assert.assertEquals("Response error", failureResult6.errorInfo.second)

        // 異常ルート（UNKNOWN）
        coEvery { deliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any()) } returns ApiResult.Failure(ApiErrorType.UNKNOWN, "")
        result = deliverFileApplyResultRepository.deliverFileApplyResult(status, errorReason, version, reserve) { _, _ -> "M-DFAR07" to "Unknown error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult7 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR07", failureResult7.errorInfo.first)
        Assert.assertEquals("Unknown error", failureResult7.errorInfo.second)
    }
}
