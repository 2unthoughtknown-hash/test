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
import java.net.SocketTimeoutException

class DeliverFileApplyResultApiTest {

    private lateinit var deliverFileApplyResultApi: DeliverFileApplyResultApi
    private lateinit var mockMsgwRepository: MsgwRepository
    private lateinit var mockPaxInfoUtil: PaxInfoUtil
    private lateinit var mockDataStoreRepository: DataStoreRepository

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(DeliverFileApplyResult.Response::class.java)

    private val serialNo = "1234567890"

    // テストデータ
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

    private val deliverFileApplyResultResponse = DeliverFileApplyResult.Response(
        status = "1000"
    )

    private val jsonString = jsonAdapter.toJson(deliverFileApplyResultResponse)

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
    fun deliverFileApplyResult_success() = runTest {
        createMockSuccess()

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Success)
        val response = (result as ApiResult.Success).response
        Assert.assertEquals(deliverFileApplyResultResponse, response)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_success_with_null_params() = runTest {
        createMockSuccess()

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, null, version, null)
        Assert.assertTrue(result is ApiResult.Success)
        val response = (result as ApiResult.Success).response
        Assert.assertEquals(deliverFileApplyResultResponse, response)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_connect_failed() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(false)

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 0) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_authenticate_failed() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(
            false,
            msgwStatus = MsgwStatus.AUTHENTICATION_ERROR
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            MsgwStatus.AUTHENTICATION_ERROR.code,
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_authenticate_failed_exception() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(
            false,
            exception = Exception(""),
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            "",
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_execCommand_json_null() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_execCommand_json_illegal() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL,
            jsonString = "111"
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.PARSE_ERROR, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_execCommand_status_error() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            false,
            msgwStatus = MsgwStatus.STATUS_ERROR
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            MsgwStatus.STATUS_ERROR.code,
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_execCommand_timeout() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            false,
            exception = SocketTimeoutException()
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.TIMEOUT, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_execCommand_fail_unknown() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false)

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
    }

    @Test
    fun deliverFileApplyResult_response_status_not_start_with_1() = runTest {
        createMockSuccess()
        
        // statusが4桁だが"1"から始まらないレスポンスを設定
        val invalidStatusResponse = DeliverFileApplyResult.Response(status = "2000")
        val invalidJsonString = jsonAdapter.toJson(invalidStatusResponse)
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL,
            jsonString = invalidJsonString
        )

        val result = deliverFileApplyResultApi.deliverFileApplyResult(status, errorReason, version, reserve)
        Assert.assertTrue(result is ApiResult.Failure)
        val failureResult = result as ApiResult.Failure
        Assert.assertEquals(ApiErrorType.RESPONSE_ERROR, failureResult.errorType)
        Assert.assertEquals("2000", failureResult.msgwErrorCode)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_APPLY_RESULT, any()) }
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
