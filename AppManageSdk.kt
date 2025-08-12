　　override suspend fun setDeliverFileInfo(deliverFileInfo: DeliverFileInfo): Result<Unit> {
        LogUtil.i(TAG, "setDeliverFileInfo() called with item: $deliverFileInfo")
        val response = binder.ensureServiceConnected().setDeliverFileInfo(deliverFileInfo)
        return parseApiResponse(response, Unit::class.java)
    }
