    @Test
    fun `testSetDeliverFileInfo Success`() = runTest {
        every { mockService.setDeliverFileInfo(any()) } returns COMMON_UNIT_SUCCESS

        val deliverFileInfo = DeliverFileInfo(
            dataType = "InFW",
            version = "01.00.01", 
            fileName = "firmware_v1.bin",
            filePath = "/system/update/firmware_v1.bin",
            autoUpdateDate = "20241201",
            manualUpdateFlag = "1",
            enable = "1",
            downloadStatus = "1",
            updateStatus = "0",
            rebootFlag = "1",
            downloadDateTime = "20241201120000"
        )
        val result = sdk.setDeliverFileInfo(deliverFileInfo)
        assert(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
    }

    @Test
    fun `testSetDeliverFileInfo Failure`() = runTest {
        every { mockService.setDeliverFileInfo(any()) } returns COMMON_FAILURE_NETWORK

        val deliverFileInfo = DeliverFileInfo(
            dataType = "InAP",
            version = "01.00.02",
            fileName = "application_v2.dat", 
            filePath = null,
            autoUpdateDate = "20241202",
            manualUpdateFlag = "0",
            enable = "1", 
            downloadStatus = "0",
            updateStatus = "0",
            rebootFlag = "0",
            downloadDateTime = null
        )
        val result = sdk.setDeliverFileInfo(deliverFileInfo)
        assert(result is Result.Failure)
        result as Result.Failure
        assertEquals("M-APUA01", result.errorCode)
        assertEquals("ネットワーク接続に失敗しました。", result.errorMessage)
    }

    @Test
    fun `testUpdateDeliverFileInfo Success`() = runTest {
        every { mockService.updateDeliverFileInfo(any(), any(), any(), any(), any()) } returns COMMON_UNIT_SUCCESS

        val result = sdk.updateDeliverFileInfo("InFW", "01.00.01", "/sdcard/update/firmware.bin", "1", "20241201120000")
        assert(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
    }

    @Test
    fun `testUpdateDeliverFileInfo Failure`() = runTest {
        every { mockService.updateDeliverFileInfo(any(), any(), any(), any(), any()) } returns COMMON_FAILURE_NETWORK

        val result = sdk.updateDeliverFileInfo("InFW", "01.00.01", "/sdcard/update/firmware.bin", "2", "20241201120000")
        assert(result is Result.Failure)
        result as Result.Failure
        assertEquals("M-APUA01", result.errorCode)
        assertEquals("ネットワーク接続に失敗しました。", result.errorMessage)
    }
