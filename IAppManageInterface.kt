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


    /**
     * 配信ファイル情報の更新処理をリクエストする。<br>
     *
     * @param dataType データ種別。
     * @param version 配信ファイルのバージョン。
     * @param filePath 配信ファイルのファイルパス。
     * @param downloadStatus DL状態（"0"：未DL、"1"：DL済、"2"：DL失敗）。
     * @param downloadDateTime DL日時（yyyyMMddhhmmss形式）。
     * @return
     * 処理結果を [Result] 型で返却する。
     * - 成功時：Result.Success(Unit) を返す。
     * - 失敗時：Result.Failure(errorCode, errorMessage) でエラーコードとエラーメッセージを返す。
     *
     * @throws IllegalStateException サービスのバインドにしていない場合。
     */
    suspend fun updateDeliverFileInfo(
        dataType: String,
        version: String,
        filePath: String,
        downloadStatus: String,
        downloadDateTime: String
    ): Result<Unit>
