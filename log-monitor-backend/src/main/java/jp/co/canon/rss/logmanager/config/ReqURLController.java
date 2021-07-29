package jp.co.canon.rss.logmanager.config;

public class ReqURLController {
    // Analysis Tool Api Controller URL
    public static final String API_DEFAULT_ANALYSIS_URL = "/api";
    public static final String API_GET_EQUIPMENTS = "/equipments";
    public static final String API_GET_LOGDATA = "/date/{log_name}/{equipment}";
    public static final String API_GET_LOGMANAGER_CONNECTION = "/connection";
    public static final String API_GET_LOGTIME = "/log/{equipment}/{log_name}";

    // Cras Server Api Controller URL
    public static final String API_DEFAULT_CRAS_SERVER_URL = "/api/cras";
    public static final String API_GET_DOWNLOAD_APP = "/app";

    // File Controller URL
    public static final String API_POST_UPLOADFILE = "/uploadFile";

    // History Controller URL
    public static final String API_DEFAULT_HISTORY_URL = "/api/v1/history/";
    public static final String API_GET_BUILD_LOG_LIST = "/{jobType}/{jobId}/{flag}";
    public static final String API_GET_BUILD_LOG_DETAIL = "/{jobType}/{jobId}/{flag}/{id}";

    // Host Controller URL
    public static final String API_DEFAULT_HOST_URL = "/api/v1/host";
    public static final String API_GET_SETTING_DB_INFO = "";

    // Job Controller URL
    public static final String API_DEFAULT_JOB_URL = "/api/v1/job";
    public static final String API_GET_REMOTE_JOB_LIST = "/remote";
    public static final String API_GET_REMOTE_JOB_DETAIL = "/remote/{id}";
    public static final String API_POST_NEW_REMOTE_JOB = "/remote";
    public static final String API_DELETE_REMOTE_JOB = "/remote/{id}";
    public static final String API_PUT_REMOTE_JOB = "/remote/{id}";
    public static final String API_GET_REMOTE_JOB_STATUS = "/remote/{id}/status";
    public static final String API_PATCH_REMOTE_JOB_RUN = "/remote/{id}/run";
    public static final String API_PATCH_REMOTE_JOB_STOP = "/remote/{id}/stop";
    public static final String API_GET_LOCAL_JOB_LIST = "/local";
    public static final String API_POST_NEW_LOCAL_JOB = "/local";
    public static final String API_DELETE_LOCAL_JOB = "/local/{id}";

    // Site Controller URL
    public static final String API_DEFAULT_SITE_URL = "/api/v1/site";
    public static final String API_GET_PLAN_LIST = "/{id}/plan";
    public static final String API_GET_SITE_NAME = "/name";
    public static final String API_GET_ALL_SITE_LIST = "";
    public static final String API_GET_SITE_DETAIL = "/{id}";
    public static final String API_POST_ADD_NEW_SITE = "";
    public static final String API_PUT_SITE_INFO = "/{id}";
    public static final String API_GET_JOB_STATUS = "/{siteId}/jobstatus";
    public static final String API_DEL_SITE = "/{id}";
    public static final String API_POST_CRAS_CONNECTION = "/connection/cras";
    public static final String API_POST_RSS_CONNECTION = "/connection/rss";
    public static final String API_POST_EMAIL_CONNECTION = "/connection/email";

    // Upload Controller URL
    public static final String API_DEFAULT_UPLOAD_URL = "/api/v1/upload";
    public static final String API_POST_LOCALFILE = "";

    // Version Controller URL ReqURLController
    public static final String API_DEFAULT_VERSION_URL = "/api/v1/version";
    public static final String API_GET_SERVER_VERSION = "";

    // Call Cras Server : AnalysisToolApiController
    public static final String API_GET_ALL_MPA_LIST = "http://%s:%s/api/rapid/equipment";
    public static final String API_GET_LOG_DATA_TIME = "http://%s:%s/api/converter/log/%s?equipment=%s";
    public static final String API_GET_LOG_DATA = "http://%s:%s/api/converter/log/dump/%s?start=%s&end=%s&equipment=%s";

    // Call Cras Server : ConfingureController
    public static final String API_GET_PLAN_LIST_FROM_CRAS = "http://%s:%s/api/rapid/plan?host=%s&port=%s&user=%s&pass=%s";
    public static final String API_GET_CRAS_CONNECTION = "http://%s:%s/api";
    public static final String API_GET_RSS_CONNECTION = "http://%s:%d/api/rapid/valid?host=%s&port=%d&user=%s&password=%s";

    // Call Cras Server : StatusController
    public static final String API_GET_BUILD_LOG_LIST_CONVERT = "http://%s:%s/api/converter/history";
    public static final String API_GET_BUILD_LOG_LIST_ERROR = "http://%s:%s/api/summary/history";
    public static final String API_GET_BUILD_LOG_LIST_CARS = "http://%s:%s/api/cras/history";
    public static final String API_GET_BUILD_LOG_LIST_VERSION ="http://%s:%s/api/version/history";

    public static final String API_GET_BUILD_LOG_LIST_CONVERT_DETAIL = "http://%s:%s/api/converter/history/%s";
    public static final String API_GET_BUILD_LOG_LIST_ERROR_DETAIL = "http://%s:%s/api/summary/history/%s";
    public static final String API_GET_BUILD_LOG_LIST_CARS_DETAIL = "http://%s:%s/api/cras/history/%s";
    public static final String API_GET_BUILD_LOG_LIST_VERSION_DETAIL ="http://%s:%s/api/version/history/%s";
}
