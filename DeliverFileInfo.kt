package com.nec.appmanage.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 配信ファイル登録情報を保持するデータクラス。
 *
 * @property dataType データ種別
 * @property version 配信ファイルのバージョン
 * @property fileName 配信ファイルのファイル名
 * @property filePath 配信ファイルのファイルパス
 * @property autoUpdateDate 自動適用日（形式：yyyyMMdd）
 * @property manualUpdateFlag 手動適用フラグ（"0"：自動更新、"1"：手動更新）
 * @property enable 有効判定（"0"：無効、"1"：有効）
 * @property downloadStatus DL状態（"0"：未DL、"1"：DL済、"2"：DL失敗）
 * @property updateStatus 更新状態
 * @property rebootFlag 再起動フラグ（"0"：再起動不要、"1"：再起動必要）
 * @property downloadDateTime DL日時（形式：yyyyMMddhhmmss）
 */
@Parcelize
data class DeliverFileInfo(
    val dataType: String,
    val version: String,
    val fileName: String,
    val filePath: String?,
    val autoUpdateDate: String,
    val manualUpdateFlag: String,
    val enable: String,
    val downloadStatus: String,
    val updateStatus: String,
    val rebootFlag: String,
    val downloadDateTime: String?
) : Parcelable
