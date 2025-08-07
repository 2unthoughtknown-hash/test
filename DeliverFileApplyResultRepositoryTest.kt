package com.nec.highfuncmiddle.repository

import android.util.Log
import com.nec.highfuncmiddle.data.api.ApiErrorType
import com.nec.highfuncmiddle.data.api.ApiResult
import com.nec.highfuncmiddle.data.api.DeliverFileApplyResultApi
import com.nec.highfuncmiddle.data.msgw.DeliverFileApplyResult
import com.nec.highfuncmiddle.data.msgw.MsgwStatus
import com.nec.highfuncmiddle.domain.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
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
    private lateinit var mockDeliverFileApplyResultApi: DeliverFileApplyResultApi

    // テストデータ
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

    private val response = DeliverFileApplyResult.Response(status = "1000")

    @Before
    fun setUp() = runTest {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0

        mockDeliverFileApplyResultApi = mockk<DeliverFileApplyResultApi>()
        deliverFileApplyResultRepository = DeliverFileApplyResultRepository(mockDeliverFileApplyResultApi)
    }

    @After
    fun tearDown() = runTest {
        unmockkAll()
    }

    @Test
    fun deliverFileApplyResult_success() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Success(response)

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            counter++
            "a" to "b"
        }
        Assert.assertEquals(0, counter)

        Assert.assertTrue(result is UseCaseResult.Success)
        val useCaseResult = result as UseCaseResult.Success
        Assert.assertEquals("1000", useCaseResult.data)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_success_with_null_params() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Success(response)

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, null, version, null
        ) { errorType, msgwErrorCode ->
            counter++
            "a" to "b"
        }
        Assert.assertEquals(0, counter)

        Assert.assertTrue(result is UseCaseResult.Success)
        val useCaseResult = result as UseCaseResult.Success
        Assert.assertEquals("1000", useCaseResult.data)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, null, version, null)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_connection_failed() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.CONNECTION_FAILED, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR01" to "Connection failed"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR01", useCaseResult.errorInfo.first)
        Assert.assertEquals("Connection failed", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_timeout() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.TIMEOUT, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.TIMEOUT, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR02" to "Timeout error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR02", useCaseResult.errorInfo.first)
        Assert.assertEquals("Timeout error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_server_error() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.SERVER_ERROR, MsgwStatus.AUTHENTICATION_ERROR.code)

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.SERVER_ERROR, errorType)
            Assert.assertEquals(MsgwStatus.AUTHENTICATION_ERROR.code, msgwErrorCode)
            counter++
            "M-DFAR03" to "Server error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR03", useCaseResult.errorInfo.first)
        Assert.assertEquals("Server error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_parse_error() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.PARSE_ERROR, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.PARSE_ERROR, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR04" to "Parse error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR04", useCaseResult.errorInfo.first)
        Assert.assertEquals("Parse error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_network_unavailable() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.NETWORK_UNAVAILABLE, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.NETWORK_UNAVAILABLE, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR05" to "Network unavailable"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR05", useCaseResult.errorInfo.first)
        Assert.assertEquals("Network unavailable", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_response_error() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.RESPONSE_ERROR, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.RESPONSE_ERROR, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR06" to "Response error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR06", useCaseResult.errorInfo.first)
        Assert.assertEquals("Response error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_unknown() = runTest {
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.UNKNOWN, "")

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.UNKNOWN, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DFAR07" to "Unknown error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR07", useCaseResult.errorInfo.first)
        Assert.assertEquals("Unknown error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_failure_with_msgw_error_code() = runTest {
        val msgwErrorCode = "2200"
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Failure(ApiErrorType.SERVER_ERROR, msgwErrorCode)

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.SERVER_ERROR, errorType)
            Assert.assertEquals("2200", msgwErrorCode)
            counter++
            "M-DFAR08" to "Server error with code"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DFAR08", useCaseResult.errorInfo.first)
        Assert.assertEquals("Server error with code", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }

    @Test
    fun deliverFileApplyResult_different_status_response() = runTest {
        val differentStatusResponse = DeliverFileApplyResult.Response(status = "9999")
        coEvery {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(any(), any(), any(), any())
        } returns ApiResult.Success(differentStatusResponse)

        var counter = 0
        val result = deliverFileApplyResultRepository.deliverFileApplyResult(
            status, errorReason, version, reserve
        ) { errorType, msgwErrorCode ->
            counter++
            "a" to "b"
        }
        Assert.assertEquals(0, counter)

        Assert.assertTrue(result is UseCaseResult.Success)
        val useCaseResult = result as UseCaseResult.Success
        Assert.assertEquals("9999", useCaseResult.data)

        coVerify(exactly = 1) {
            mockDeliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        }
    }
}
