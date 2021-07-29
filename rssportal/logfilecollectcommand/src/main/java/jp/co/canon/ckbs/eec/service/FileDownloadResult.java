package jp.co.canon.ckbs.eec.service;

public enum FileDownloadResult {
    RESULT_NONE,
    RESULT_COMPLETED,
    RESULT_COMPLETED_SKIP_RECONNECT,
    RESULT_RETRY_PERMANENT,
    RESULT_RETRY_TRANSIENT,
    RESULT_FAIL
}
