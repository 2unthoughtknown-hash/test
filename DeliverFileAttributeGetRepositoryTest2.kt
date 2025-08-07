package com.nec.highfuncmiddle.repository

import android.util.Log
import com.nec.highfuncmiddle.data.api.ApiErrorType
import com.nec.highfuncmiddle.data.api.ApiResult
import com.nec.highfuncmiddle.data.api.DeliverFileAttributeGetApi
import com.nec.highfuncmiddle.data.msgw.DeliverFileAttributeGet
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

class DeliverFileAttributeGetRepositoryTest {
    private lateinit var deliverFileAttributeGetRepository: DeliverFileAttributeGetRepository
    private lateinit var deliverFileAttributeGetApi: DeliverFileAttributeGetApi

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

    private val json = DeliverFileAttributeGet.Response(
        count = "3",
        list = attributeList
    )

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

        mockkStatic(DeliverFileAttributeGetRepository::class)

        deliverFileAttributeGetApi = mockk<DeliverFileAttributeGetApi>()
        deliverFileAttributeGetRepository = DeliverFileAttributeGetRepository(deliverFileAttributeGetApi)
    }

    @After
    fun tearDown() = runTest {
        unmockkAll()
    }

    @Test
    fun getDeliverFileAttributeTest() = runTest {
        val saveError: suspend (ApiErrorType, String) -> Pair<String, String> = mockk()

        // 正常ルート（通常のリスト）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Success(json)
        var result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList, saveError)
        Assert.assertTrue(result is UseCaseResult.Success)
        val successResult = result as UseCaseResult.Success
        Assert.assertEquals(attributeList, successResult.data)

        // 正常ルート（空のリスト）
        val emptyFileInfoList = emptyList<DeliverFileAttributeGet.Info>()
        val emptyJson = DeliverFileAttributeGet.Response(count = "0", list = emptyList())
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Success(emptyJson)
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(emptyFileInfoList, saveError)
        Assert.assertTrue(result is UseCaseResult.Success)
        val successResult2 = result as UseCaseResult.Success
        Assert.assertEquals(emptyList<DeliverFileAttributeGet.Attribute>(), successResult2.data)

        // 異常ルート（CONNECTION_FAILED）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.CONNECTION_FAILED, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA01" to "Connection failed" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult1 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA01", failureResult1.errorInfo.first)
        Assert.assertEquals("Connection failed", failureResult1.errorInfo.second)

        // 異常ルート（TIMEOUT）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.TIMEOUT, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA02" to "Timeout error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult2 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA02", failureResult2.errorInfo.first)
        Assert.assertEquals("Timeout error", failureResult2.errorInfo.second)

        // 異常ルート（SERVER_ERROR）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.SERVER_ERROR, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA03" to "Server error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult3 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA03", failureResult3.errorInfo.first)
        Assert.assertEquals("Server error", failureResult3.errorInfo.second)

        // 異常ルート（PARSE_ERROR）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.PARSE_ERROR, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA04" to "Parse error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult4 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA04", failureResult4.errorInfo.first)
        Assert.assertEquals("Parse error", failureResult4.errorInfo.second)

        // 異常ルート（NETWORK_UNAVAILABLE）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.NETWORK_UNAVAILABLE, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA05" to "Network unavailable" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult5 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA05", failureResult5.errorInfo.first)
        Assert.assertEquals("Network unavailable", failureResult5.errorInfo.second)

        // 異常ルート（RESPONSE_ERROR）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.RESPONSE_ERROR, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA06" to "Response error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult6 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA06", failureResult6.errorInfo.first)
        Assert.assertEquals("Response error", failureResult6.errorInfo.second)

        // 異常ルート（UNKNOWN）
        coEvery { deliverFileAttributeGetApi.getDeliverFileAttribute(any()) } returns ApiResult.Failure(ApiErrorType.UNKNOWN, "")
        result = deliverFileAttributeGetRepository.getDeliverFileAttribute(fileInfoList) { _, _ -> "M-DAFA07" to "Unknown error" }
        Assert.assertTrue(result is UseCaseResult.Failure)
        val failureResult7 = result as UseCaseResult.Failure
        Assert.assertEquals("M-DAFA07", failureResult7.errorInfo.first)
        Assert.assertEquals("Unknown error", failureResult7.errorInfo.second)
    }
}
