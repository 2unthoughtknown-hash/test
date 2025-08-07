package com.nec.highfuncmiddle.data.api

import android.util.Log
import com.nec.highfuncmiddle.common.utli.PaxInfoUtil
import com.nec.highfuncmiddle.data.datastore.DataStoreKeys
import com.nec.highfuncmiddle.data.msgw.DeliverFileAttributeGet
import com.nec.highfuncmiddle.data.msgw.MsgwBusinessType
import com.nec.highfuncmiddle.data.msgw.MsgwRequestType
import com.nec.highfuncmiddle.data.msgw.MsgwResult
import com.nec.highfuncmiddle.data.msgw.MsgwStatus
import com.nec.highfuncmiddle.data.repository.MsgwRepository
import com.nec.highfuncmiddle.repository.DataStoreRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
import java.net.SocketTimeoutException

class DeliverFileAttributeGetApiTest {

    private lateinit var deliverFileAttributeGetApi: DeliverFileAttributeGetApi
    private lateinit var mockMsgwRepository: MsgwRepository
    private lateinit var mockPaxInfoUtil: PaxInfoUtil
    private lateinit var mockDataStoreRepository: DataStoreRepository

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(DeliverFileAttributeGet.Response::class.java)

    private val serialNo = "1234567890"
    private val fileInfoList = listOf(
        DeliverFileAttributeGet.Info("IM28MGOS", "1.0.0"),
        DeliverFileAttributeGet.Info("IM28MGAP", "2.0.0")
    )

    private val deliverFileAttributeResponse = DeliverFileAttributeGet.Response(
        count = "2",
        list = listOf(
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
            )
        )
    )

    private val jsonString = jsonAdapter.toJson(deliverFileAttributeResponse)

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

        mockPaxInfoUtil = mockk<PaxInfoUtil>()
        mockDataStoreRepository = mockk<DataStoreRepository>()
        mockMsgwRepository = mockk<MsgwRepository>()

        deliverFileAttributeGetApi = DeliverFileAttributeGetApi(mockMsgwRepository)
        deliverFileAttributeGetApi.paxInfoUtil = mockPaxInfoUtil
        deliverFileAttributeGetApi.dataStoreRepository = mockDataStoreRepository

        every { mockPaxInfoUtil.serial } returns serialNo
        coEvery { mockDataStoreRepository.get<String>(any()) } returns "12345678901234567890"
        every { mockMsgwRepository.init(any(), any(), any()) } returns Unit
        every { mockMsgwRepository.setMsrwParameter(any(), any()) } returns Unit
    }

    @After
    fun tearDown() = runTest {
        unmockkAll()
    }

    @Test
    fun getDeliverFileAttributeTest() = runTest {
        // 正常ルート
        createMockSuccess()
        var result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Success)
        var successResult = result as ApiResult.Success
        Assert.assertEquals("2", successResult.response.count)
        Assert.assertEquals(2, successResult.response.list.size)

        // 異常ルート
        // 接続失敗
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(false)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        var failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, failureResult.errorType)

        // 認証失敗 (msgwStatus付き)
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, MsgwStatus.AUTHENTICATION_ERROR)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)
        Assert.assertEquals(MsgwStatus.AUTHENTICATION_ERROR.code, failureResult.msgwErrorCode)

        // 認証失敗 (msgwStatus null)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, null)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)

        // 認証失敗 (例外発生)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, exception = Exception(""))
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)
        Assert.assertEquals("", failureResult.msgwErrorCode)

        // コマンド実行失敗 (通常の失敗)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, msgwStatus = MsgwStatus.NORMAL, jsonString = jsonString)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)

        // コマンド実行失敗 (ステータスエラー)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, msgwStatus = MsgwStatus.STATUS_ERROR)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)
        Assert.assertEquals(MsgwStatus.STATUS_ERROR.code, failureResult.msgwErrorCode)

        // コマンド実行失敗 (タイムアウト)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, exception = SocketTimeoutException())
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.TIMEOUT, failureResult.errorType)

        // コマンド実行失敗 (不明なエラー)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.UNKNOWN, failureResult.errorType)

        // JSONレスポンスがnull
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.UNKNOWN, failureResult.errorType)

        // JSON解析エラー
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL, jsonString = "111")
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.PARSE_ERROR, failureResult.errorType)

        // 切断失敗（正常処理継続）
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL, jsonString = jsonString)
        coEvery { mockMsgwRepository.disconnect() } returns MsgwResult(false)
        result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Success)
        successResult = result as ApiResult.Success
        Assert.assertEquals("2", successResult.response.count)
    }

    private fun createMockSuccess() {
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.disconnect() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL,
            jsonString = jsonString
        )
    }
}
