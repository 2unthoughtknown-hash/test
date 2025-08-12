　　　/**
     * 配信ファイル登録情報の反映処理をリクエストする。<br>
     *
     * @param deliverFileInfo 配信ファイル登録情報。
     * @return
     * 処理結果を [Result] 型で返却する。
     * - 成功時：Result.Success(Unit) を返す。
     * - 失敗時：Result.Failure(errorCode, errorMessage) でエラーコードとエラーメッセージを返す。
     *
     * @throws IllegalStateException サービスのバインドにしていない場合。
     */
    suspend fun setDeliverFileInfo(deliverFileInfo: DeliverFileInfo): Result<Unit>
