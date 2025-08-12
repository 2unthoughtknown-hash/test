import com.nec.appmanage.sdk.DeliverFileInfo;

    /**
     * 配信ファイル登録処理をリクエストする。<br>
     *
     * @param deliverFileInfo 配信ファイル登録情報
     * @return JSON文字列で処理結果を返却する。
     */
    String setDeliverFileInfo(in DeliverFileInfo deliverFileInfo);

    /**
     * 配信ファイル情報の更新処理をリクエストする。<br>
     *
     * @param dataType データ種別
     * @param version 配信ファイルのバージョン
     * @param filePath 配信ファイルのファイルパス
     * @param downloadStatus DL状態（"0"未DL、"1"DL済、"2"DL失敗）
     * @param downloadDateTime DL日時（yyyyMMddhhmmss形式）
     * @return JSON文字列で処理結果を返却する。
     */
    String updateDeliverFileInfo(String dataType, String version, String filePath, String downloadStatus, String downloadDateTime);
