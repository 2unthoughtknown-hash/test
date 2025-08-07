package com.nec.highfuncmiddle.repository

import android.util.Log
import com.nec.highfuncmiddle.data.api.ApiErrorType
import com.nec.highfuncmiddle.data.api.ApiResult
import com.nec.highfuncmiddle.data.api.DeliverFileAttributeGetApi
import com.nec.highfuncmiddle.data.msgw.DeliverFileAttributeGet
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

class DeliverFileAttributeGetRepositoryTest {
    private lateinit var deliverFileAttributeGetRepository: DeliverFileAttributeGetRepository
    private lateinit var mockDeliverFileAttributeGetApi: DeliverFileAttributeGetApi

    // テストデータ
    private val fileInfoList = listOf(
        DeliverFileAttributeGet.Info("IM28MGOS", "1.0.0"),
        DeliverFileAttributeGet.Info("IM28MGAP", "2.0.0"),
        DeliverFileAttributeGet.Info("IM28SBOS", "1.5.0")
    )

    private val attributeList = listOf(
        DeliverFileAttributeGet.Attribute(
            dataType = "IM28MGOS",
            ver = "1.0.0",
            autoUpdateDate = "20250128120000",
            manualUpdateFlag = "1",
            enable = "1",
            rebootFlag = "0"
        ),
        DeliverFileAttributeGet.Attribute(
            dataType = "IM28MGAP",
            ver = "2.0.0",
            autoUpdateDate = "20250128130000",
            manualUpdateFlag = "0",
            enable = "1",
            rebootFlag = "1"
        ),
        DeliverFileAttributeGet.Attribute(
            dataType = "IM28SBOS",
            ver = "1.5.0",
            autoUpdateDate = "20250128140000",
            manualUpdateFlag = "1",
            enable = "0",
            rebootFlag = "0"
        )
    )

    private val response = DeliverFileAttributeGet.Response(
        count = "3",
        list = attributeList
    )

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

        mockDeliverFileAttributeGetApi = mockk<DeliverFileAttributeGetApi>()
        deliverFileAttributeGetRepository = DeliverFileAttributeGetRepository(mockDeliverFileAttributeGetApi)
    }

    @After
    fun tearDown() = runTest {
        unmockkAll()
    }

    @Test
    fun getDeliverFileAttribute_success() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Success(response)

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            counter++
            "a" to "b"
        }
        Assert.assertEquals(0, counter)

        Assert.assertTrue(result is UseCaseResult.Success)
        val useCaseResult = result as UseCaseResult.Success
        Assert.assertEquals(attributeList, useCaseResult.data)

        // APIが正しく呼び出されることを確認
        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_success_empty_list() = runTest {
        val emptyFileInfoList = emptyList<DeliverFileAttributeGet.Info>()
        val emptyResponse = DeliverFileAttributeGet.Response(
            count = "0",
            list = emptyList()
        )

        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Success(emptyResponse)

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(emptyFileInfoList) { errorType, msgwErrorCode ->
            counter++
            "a" to "b"
        }
        Assert.assertEquals(0, counter)

        Assert.assertTrue(result is UseCaseResult.Success)
        val useCaseResult = result as UseCaseResult.Success
        Assert.assertEquals(emptyList<DeliverFileAttributeGet.Attribute>(), useCaseResult.data)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(emptyFileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_connection_failed() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.CONNECTION_FAILED,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA01" to "Connection failed"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA01", useCaseResult.errorInfo.first)
        Assert.assertEquals("Connection failed", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_timeout() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.TIMEOUT,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.TIMEOUT, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA02" to "Timeout error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA02", useCaseResult.errorInfo.first)
        Assert.assertEquals("Timeout error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_server_error() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.SERVER_ERROR,
            MsgwStatus.AUTHENTICATION_ERROR.code
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.SERVER_ERROR, errorType)
            Assert.assertEquals(MsgwStatus.AUTHENTICATION_ERROR.code, msgwErrorCode)
            counter++
            "M-DAFA03" to "Server error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA03", useCaseResult.errorInfo.first)
        Assert.assertEquals("Server error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_parse_error() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.PARSE_ERROR,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.PARSE_ERROR, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA04" to "Parse error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA04", useCaseResult.errorInfo.first)
        Assert.assertEquals("Parse error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_network_unavailable() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.NETWORK_UNAVAILABLE,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.NETWORK_UNAVAILABLE, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA05" to "Network unavailable"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA05", useCaseResult.errorInfo.first)
        Assert.assertEquals("Network unavailable", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_response_error() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.RESPONSE_ERROR,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.RESPONSE_ERROR, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA06" to "Response error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA06", useCaseResult.errorInfo.first)
        Assert.assertEquals("Response error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_unknown() = runTest {
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.UNKNOWN,
            ""
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.UNKNOWN, errorType)
            Assert.assertEquals("", msgwErrorCode)
            counter++
            "M-DAFA07" to "Unknown error"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA07", useCaseResult.errorInfo.first)
        Assert.assertEquals("Unknown error", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }

    @Test
    fun getDeliverFileAttribute_failure_with_msgw_error_code() = runTest {
        val msgwErrorCode = "2200"
        coEvery { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(
            ApiErrorType.SERVER_ERROR,
            msgwErrorCode
        )

        var counter = 0
        val result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { errorType, msgwErrorCode ->
            Assert.assertEquals(ApiErrorType.SERVER_ERROR, errorType)
            Assert.assertEquals("2200", msgwErrorCode)
            counter++
            "M-DAFA08" to "Server error with code"
        }
        Assert.assertEquals(1, counter)

        Assert.assertTrue(result is UseCaseResult.Failure)
        val useCaseResult = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA08", useCaseResult.errorInfo.first)
        Assert.assertEquals("Server error with code", useCaseResult.errorInfo.second)

        coVerify(exactly = 1) { mockDeliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList) }
    }
}
