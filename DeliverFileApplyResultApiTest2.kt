package com.nec.highfuncmiddle.data.api

import android.util.Log
import com.nec.highfuncmiddle.common.utli.PaxInfoUtil
import com.nec.highfuncmiddle.data.datastore.DataStoreKeys
import com.nec.highfuncmiddle.data.msgw.DeliverFileApplyResult
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

class DeliverFileApplyResultApiTest {

    private lateinit var deliverFileApplyResultApi: DeliverFileApplyResultApi
    private lateinit var mockMsgwRepository: MsgwRepository
    private lateinit var mockPaxInfoUtil: PaxInfoUtil
    private lateinit var mockDataStoreRepository: DataStoreRepository

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(DeliverFileApplyResult.Response::class.java)

    private val serialNo = "1234567890"
    private val status = "1000"
    private val errorReason = "Test Error"
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
        iM28MGAP = listOf("2.0.1", "2.0.2", "2.0.3"),
        iM28MMAP = listOf("1.5.1"),
        iM28SBOS = listOf("1.2.1"),
        iM28SBBL = listOf("1.3.1", "1.3.2"),
        iM28SBAP = listOf("2.1.1")
    )

    private val deliverFileApplyResultResponse = DeliverFileApplyResult.Response(status = "1000")
    private val jsonString = jsonAdapter.toJson(deliverFileApplyResultResponse)

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

        deliverFileApplyResultApi = DeliverFileApplyResultApi(mockMsgwRepository)
        deliverFileApplyResultApi.paxInfoUtil = mockPaxInfoUtil
        deliverFileApplyResultApi.dataStoreRepository = mockDataStoreRepository

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
    fun deliverFileApplyResultTest() = runTest {
        // 正常ルート
        createMockSuccess()
        var result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Success)
        var successResult = result as ApiResult.Success
        Assert.assertEquals(status, successResult.response.status)

        // nullパラメータでの正常ルート
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, null, version, null)
        Assert.assertTrue(result is ApiResult.Success)
        successResult = result as ApiResult.Success
        Assert.assertEquals(status, successResult.response.status)

        // 異常ルート
        // 接続失敗
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(false)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        var failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, failureResult.errorType)

        // 認証失敗 (msgwStatus付き)
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, MsgwStatus.AUTHENTICATION_ERROR)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)
        Assert.assertEquals(MsgwStatus.AUTHENTICATION_ERROR.code, failureResult.msgwErrorCode)

        // 認証失敗 (msgwStatus null)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, null)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)

        // 認証失敗 (例外発生)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(false, exception = Exception("test"))
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)

        // コマンド実行失敗 (通常の失敗)
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(true)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, msgwStatus = MsgwStatus.NORMAL, jsonString = jsonString)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)

        // コマンド実行失敗 (ステータスエラー)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, msgwStatus = MsgwStatus.STATUS_ERROR)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, failureResult.errorType)
        Assert.assertEquals(MsgwStatus.STATUS_ERROR.code, failureResult.msgwErrorCode)

        // コマンド実行失敗 (タイムアウト)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false, exception = SocketTimeoutException())
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.TIMEOUT, failureResult.errorType)

        // コマンド実行失敗 (不明なエラー)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.UNKNOWN, failureResult.errorType)

        // JSONレスポンスがnull
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.UNKNOWN, failureResult.errorType)

        // JSON解析エラー
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL, jsonString = "111")
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.PARSE_ERROR, failureResult.errorType)

        // 切断失敗（正常処理継続）
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(true, msgwStatus = MsgwStatus.NORMAL, jsonString = jsonString)
        coEvery { mockMsgwRepository.disconnect() } returns MsgwResult(false)
        result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Success)
        successResult = result as ApiResult.Success
        Assert.assertEquals(status, successResult.response.status)
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
