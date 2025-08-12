　　override suspend fun setDeliverFileInfo(deliverFileInfo: DeliverFileInfo): Result<Unit> {
        LogUtil.i(TAG, "setDeliverFileInfo() called with item: $deliverFileInfo")
        val response = binder.ensureServiceConnected().setDeliverFileInfo(deliverFileInfo)
        return parseApiResponse(response, Unit::class.java)
    }

      override suspend fun updateDeliverFileInfo(
        dataType: String,
        version: String,
        filePath: String,
        downloadStatus: String,
        downloadDateTime: String
    ): Result<Unit> {
        LogUtil.i(TAG, "updateDeliverFileInfo() called with dataType: $dataType, version: $version, filePath: $filePath, downloadStatus: $downloadStatus, downloadDateTime: $downloadDateTime")
        val response = binder.ensureServiceConnected().updateDeliverFileInfo(dataType, version, filePath, downloadStatus, downloadDateTime)
        return parseApiResponse(response, Unit::class.java)
    }
