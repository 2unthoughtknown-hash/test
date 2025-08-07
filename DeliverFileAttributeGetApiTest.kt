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

class DeliverFileAttributeGetApiTest {

    private lateinit var deliverFileAttributeGetApi: DeliverFileAttributeGetApi
    private lateinit var mockMsgwRepository: MsgwRepository
    private lateinit var mockPaxInfoUtil: PaxInfoUtil
    private lateinit var mockDataStoreRepository: DataStoreRepository

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(DeliverFileAttributeGet.Response::class.java)

    private val serialNo = "1234567890"

    // テストデータ
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
    fun getDeliverFileAttribute_success() = runTest {
        createMockSuccess()

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Success)
        val response = (result as ApiResult.Success).response
        Assert.assertEquals(deliverFileAttributeResponse, response)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_connect_failed() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.connect() } returns MsgwResult(false)

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.CONNECTION_FAILED, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 0) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_authenticate_failed() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(
            false,
            msgwStatus = MsgwStatus.AUTHENTICATION_ERROR
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            MsgwStatus.AUTHENTICATION_ERROR.code,
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_authenticate_failed_exception() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.authenticate() } returns MsgwResult(
            false,
            exception = Exception(""),
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            "",
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 0) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 0) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_execCommand_json_null() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_execCommand_json_illegal() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            true,
            msgwStatus = MsgwStatus.NORMAL,
            jsonString = "111"
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.PARSE_ERROR, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_execCommand_status_error() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            false,
            msgwStatus = MsgwStatus.STATUS_ERROR
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.SERVER_ERROR, (result as ApiResult.Failure).errorType)
        Assert.assertEquals(
            MsgwStatus.STATUS_ERROR.code,
            (result as ApiResult.Failure).msgwErrorCode
        )

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_execCommand_timeout() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(
            false,
            exception = SocketTimeoutException()
        )

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.TIMEOUT, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
    }

    @Test
    fun getDeliverFileAttribute_execCommand_fail_unknown() = runTest {
        createMockSuccess()
        coEvery { mockMsgwRepository.execCommand(any(), any(), any()) } returns MsgwResult(false)

        val result = deliverFileAttributeGetApi.getDeliverFileAttribute(fileInfoList)
        Assert.assertTrue(result is ApiResult.Failure)
        Assert.assertEquals(ApiErrorType.UNKNOWN, (result as ApiResult.Failure).errorType)

        coVerify(exactly = 1) { mockMsgwRepository.connect() }
        coVerify(exactly = 1) { mockMsgwRepository.disconnect() }
        coVerify(exactly = 1) { mockMsgwRepository.authenticate() }
        coVerify(exactly = 1) { mockMsgwRepository.execCommand(MsgwRequestType.TERM_REQ, MsgwBusinessType.DELIVER_FILE_ATTRIBUTE_GET, any()) }
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
