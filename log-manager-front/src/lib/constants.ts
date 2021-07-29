export const breadcrumbLocation = {
  status_remote: ['Status', 'Remote'],
  status_local: ['Status', 'Remote'],
  configure: ['Configure'],
  rules_logdef: ['Rules', 'Log Definition'],
  rules_logconv: ['Rules', 'Log Converter'],
};

export const LOG_HISTORY_MAX_LIST_COUNT = 50;
export const ERROR_SUMMARY_DEFAULT_BEFORE = 7;
export const CRAS_DATA_DEFAULT_BEFORE = 30;
export const MPA_VERSION_DEFAULT_BEFORE = 7;
export const EMAIL_ADDRESS_MAX = 100;
export const EMAIL_SUBJECT_MAX = 255;
export const EMAIL_BEFORE_MAX = 365;
export const CONFIGURE_NAME_MAX = 30;
export const DEFAULT_PASSWORD_VALUE = '******';
export const ACCESS_TOKEN_NAME = 'access-token';
export const REFRESH_TOKEN_NAME = 'refresh-token';
export const TOKEN_PATH = '/';
export const SESSION_STORAGE_EXPIRED = 'expired';
export const CRAS_LOCALHOST_NAME = 'cras-server';
export const DEAFULT_URL = '/logmonitor';

export const PAGE_URL = {
  LOGIN_ROUTE: DEAFULT_URL + '/login',
  STATUS_ROUTE: DEAFULT_URL + '/status',
  STATUS_REMOTE_ROUTE: DEAFULT_URL + '/status/remote',
  STATUS_REMOTE_ADD_ROUTE: DEAFULT_URL + '/status/remote/add',
  STATUS_REMOTE_EDIT_ROUTE: DEAFULT_URL + '/status/remote/edit/:jobid',
  STATUS_REMOTE_BUILD_HISTORY_ROUTE: DEAFULT_URL + '/status/remote/history/:type/:id',
  STATUS_LOCAL_ROUTE: DEAFULT_URL + '/status/local',
  STATUS_LOCAL_ADD_ROUTE: DEAFULT_URL + '/status/local/add',
  STATUS_LOCAL_BUILD_HISTORY_ROUTE: DEAFULT_URL + '/status/local/history/convert/:id',
  CONFIGURE_ROUTE: DEAFULT_URL + '/configure',
  RULES_ROUTE: DEAFULT_URL + '/rules',
  RULES_CONVERT_RULES: DEAFULT_URL + '/rules/convert-log',
  RULES_CRAS_DATA_ROUTE: DEAFULT_URL + '/rules/cras-data',
  RULES_CRAS_DATA_EDIT_CREATE_ROUTE: DEAFULT_URL + '/rules/cras-data/create/:siteId',
  RULES_CRAS_DATA_EDIT_JUDGE_ROUTE: DEAFULT_URL + '/rules/cras-data/judge/:siteId',
  ADDRESS_BOOK_ROUTE: DEAFULT_URL + '/address',
  ACCOUNT_ROUTE: DEAFULT_URL + '/account',
  FORBBIDEN_ROUTE: DEAFULT_URL + '/forbbiden',

  STATUS_LOCAL: DEAFULT_URL + '/status/local',
  STATUS_LOCAL_ADD: DEAFULT_URL + '/status/local/add',
  STATUS_LOCAL_BUILD_HISTORY_CONVERT: DEAFULT_URL + '/status/local/history/convert',
  STATUS_REMOTE: DEAFULT_URL + '/status/remote',
  STATUS_REMOTE_ADD: DEAFULT_URL + '/status/remote/add',
  STATUS_REMOTE_EDIT: ({
    jobid,
    siteId,
    siteName,
  }: {
    jobid: string | number;
    siteId: string | number;
    siteName: string;
  }): string => DEAFULT_URL + `/status/remote/edit/${jobid}?id=${siteId}&name=${siteName}`,
  STATUS_REMOTE_BUILD_HISTORY: ({
    type,
    id,
    siteName,
  }: {
    type: string;
    id: string | number;
    siteName: string;
  }): string => DEAFULT_URL + `/status/remote/history/${type}/${id}?name=${siteName}`,
  STATUS_REMOTE_BUILD_HISTORY_CONVERT: DEAFULT_URL + '/status/remote/history/convert',
  STATUS_REMOTE_BUILD_HISTORY_ERROR: DEAFULT_URL + '/status/remote/history/error',
  STATUS_REMOTE_BUILD_HISTORY_CRAS: DEAFULT_URL + '/status/remote/history/cras',
  STATUS_REMOTE_BUILD_HISTORY_VERSION: DEAFULT_URL + '/status/remote/history/version',
  CONFIGURE: DEAFULT_URL + '/configure',
  ADDRESS_BOOK: DEAFULT_URL + '/address',
  ACCOUNT: DEAFULT_URL + '/account',
  RULES_CRAS_DATA_EDIT_CREATE: (siteId: string | number, name: string): string =>
    DEAFULT_URL + `/rules/cras-data/create/${siteId}?name=${name}`,
  RULES_CRAS_DATA_EDIT_JUDGE: (siteId: string | number, name: string): string =>
    DEAFULT_URL + `/rules/cras-data/judge/${siteId}?name=${name}`,
};

export const API_URL = {
  UPLOAD_STATUS_LOCAL_JOB_FILE_URL: DEAFULT_URL + '/api/v1/upload',

  GET_STATUS_BUILD_HISTORY_LIST: ({
    type,
    jobId,
    stepType,
  }: {
    type: string;
    jobId: string | number;
    stepType: string;
  }): string => DEAFULT_URL + `/api/v1/history/${type}/${jobId}/${stepType}`,
  GET_STATUS_BUILD_HISTORY_LOG: ({
    type,
    jobId,
    stepType,
    id,
  }: {
    type: string;
    jobId: string | number;
    stepType: string;
    id: string | number;
  }): string => DEAFULT_URL + `/api/v1/history/${type}/${jobId}/${stepType}/${id}`,

  GET_STATUS_REMOTE_JOB_LIST: DEAFULT_URL + '/api/v1/job/remote',
  GET_STATUS_REMOTE_JOB_DETAIL: (id: string | number): string => DEAFULT_URL + `/api/v1/job/remote/${id}`,
  GET_STATUS_REMOTE_JOB_STOP_STATUS: (jobId: string | number): string =>
    DEAFULT_URL + `/api/v1/job/remote/${jobId}/status`,
  GET_STATUS_REMOTE_PLAN_LIST: (id: string | number): string => DEAFULT_URL + `/api/v1/site/${id}/plan`,
  POST_STATUS_REMOTE_JOB: DEAFULT_URL + '/api/v1/job/remote',
  PUT_STATUS_REMOTE_JOB: (jodbId: string | number): string => DEAFULT_URL + `/api/v1/job/remote/${jodbId}`,
  DELETE_STATUS_REMOTE_JOB: (jodbId: string | number): string => DEAFULT_URL + `/api/v1/job/remote/${jodbId}`,
  RUN_STATUS_REMOTE_JOB: (jodbId: string | number): string => DEAFULT_URL + `/api/v1/job/remote/${jodbId}/run`,
  STOP_STATUS_REMOTE_JOB: (jodbId: string | number): string => DEAFULT_URL + `/api/v1/job/remote/${jodbId}/stop`,

  GET_STATUS_LOCAL_JOB_LIST: DEAFULT_URL + '/api/v1/job/local',
  POST_STATUS_LOCAL_JOB: DEAFULT_URL + '/api/v1/job/local',
  DELETE_STATUS_LOCAL_JOB: (jodbId: string | number): string => DEAFULT_URL + `/api/v1/job/local/${jodbId}`,

  GET_CONFIGURE_SITE_NAME: DEAFULT_URL + '/api/v1/site/name',
  GET_CONFIGURE_SITE_NAME_NOT_ADDED: DEAFULT_URL + '/api/v1/site/name?notadded=true',
  GET_CONFIGURE_SITE_DB: DEAFULT_URL + '/api/v1/site',
  POST_CONFIGURE_SITE_DB: DEAFULT_URL + '/api/v1/site',
  GET_CONFIGURE_SITE_DB_DETAIL: (siteId: string | number): string => DEAFULT_URL + `/api/v1/site/${siteId}`,
  PUT_CONFIGURE_SITE_DB: (siteId: string | number): string => DEAFULT_URL + `/api/v1/site/${siteId}`,
  DELETE_CONFIGURE_SITE_DB: (siteId: string | number): string => DEAFULT_URL + `/api/v1/site/${siteId}`,
  GET_CONFIGURE_HOST_DB: DEAFULT_URL + '/api/v1/host',
  POST_CONFIGURE_HOST_DB: DEAFULT_URL + '/api/v1/host',
  GET_CONFITURE_CRAS_CONNECTION: DEAFULT_URL + '/api/v1/site/connection/cras',
  GET_CONFITURE_EMAIL_CONNECTION: DEAFULT_URL + '/api/v1/site/connection/email',
  GET_CONFITURE_RSS_CONNECTION: DEAFULT_URL + '/api/v1/site/connection/rss',
  GET_CONFIGURE_LOG_MONITOR_VERSION: DEAFULT_URL + '/api/v1/version',
  GET_CONFIGURE_SITE_JOB_STATUS: (siteId: string | number): string => DEAFULT_URL + `/api/v1/site/${siteId}/jobstatus`,
  GET_LOG_MONITOR_OOS: DEAFULT_URL + '/LICENSE.md',

  GET_AUTH_LOGIN: (username: string, password: string): string =>
    DEAFULT_URL + `/api/v1/auth/login?username=${username}&password=${password}`,
  GET_AUTH_ME: DEAFULT_URL + '/api/v1/auth/me',
  GET_AUTH_LOGOUT: DEAFULT_URL + '/api/v1/auth/logout',
  POST_AUTH_REISSUE: DEAFULT_URL + '/api/v1/auth/reissue',

  GET_USER_LIST: DEAFULT_URL + '/api/v1/user',
  POST_USER_SIGN_UP: DEAFULT_URL + '/api/v1/user',
  DELETE_USER: (userId: string | number): string => DEAFULT_URL + `/api/v1/user/${userId}`,
  PUT_USER_ROLES: (userId: string | number): string => DEAFULT_URL + `/api/v1/user/${userId}/roles`,
  PUT_USER_PASSWORD: (userId: string | number): string => DEAFULT_URL + `/api/v1/user/${userId}/password`,

  GET_ADDRESS_GROUP_EMAIL_LIST: DEAFULT_URL + '/api/v1/address',
  GET_ADDRESS_GROUP_LIST: DEAFULT_URL + '/api/v1/address/group',
  GET_ADDRESS_GROUP_LIST_IN_EMAIL: (emailId: string | number): string =>
    DEAFULT_URL + `/api/v1/address/email/${emailId}/group`,
  GET_ADDRESS_EMAIL_LIST: DEAFULT_URL + '/api/v1/address/email',
  GET_ADDRESS_EMAIL_LIST_BY_GROUP: (groupId: string | number): string =>
    DEAFULT_URL + `/api/v1/address/group/email/${groupId}`,
  SEARCH_ADDRESS_EMAIL: (keyword: string): string => DEAFULT_URL + `/api/v1/address/search?keyword=${keyword}`,
  SEARCH_ADDRESS_GROUP_EMAIL: (keyword: string): string =>
    DEAFULT_URL + `/api/v1/address/search?keyword=${keyword}&group=true`,

  POST_ADDRESS_ADD_EMAIL: DEAFULT_URL + '/api/v1/address/email',
  DELETE_ADDRESS_DELETE_EMAIL: (emailIds: number[]): string =>
    DEAFULT_URL + `/api/v1/address/email?ids=${emailIds.toString()}`,
  PUT_ADDRESS_EDIT_EMAIL: (emailId: string | number): string => DEAFULT_URL + `/api/v1/address/email/${emailId}`,
  POST_ADDRESS_ADD_GROUP: DEAFULT_URL + '/api/v1/address/group',
  PUT_ADDRESS_EDIT_GROUP: (groupId: string | number): string => DEAFULT_URL + `/api/v1/address/group/${groupId}`,
  DELETE_ADDRESS_DELETE_GROUP: (groupId: string | number): string => DEAFULT_URL + `/api/v1/address/group/${groupId}`,

  GET_CRAS_INFO_LIST: DEAFULT_URL + '/api/v1/rule/cras',
  GET_CRAS_SITE_INFO: DEAFULT_URL + '/api/v1/rule/cras/siteinfo',
  POST_CRAS_SITE_ADD: DEAFULT_URL + '/api/v1/rule/cras',
  DELETE_CRAS_SITE_DELETE: (siteId: string | number): string => DEAFULT_URL + `/api/v1/rule/cras/${siteId}`,
  GET_CRAS_MANUAL_CREATE_INFO_LIST: (siteId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/create`,
  GET_CRAS_MANUAL_CREATE_INFO_DETAIL: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/create/${itemId}`,
  GET_CRAS_MANUAL_CREATE_TARGET_TABLE: (siteId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/table`,
  GET_CRAS_MANUAL_CREATE_TARGET_COLUMN: (siteId: string | number, name: string): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/table/${name}`,
  POST_CRAS_MANUAL_CREATE_TEST_QUERY: DEAFULT_URL + '/api/v1/rule/cras/testquery',
  POST_CRAS_MANUAL_CREATE_ADD: (siteId: string | number): string => DEAFULT_URL + `/api/v1/rule/cras/${siteId}/create`,
  PUT_CRAS_MANUAL_CREATE_EDIT: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/create/${itemId}`,
  DELETE_CRAS_MANUAL_CREATE_DELETE: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/create/${itemId}`,
  GET_CRAS_MANUAL_JUDGE_INFO_LIST: (siteId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/judge`,
  GET_CRAS_MANUAL_JUDGE_INFO_DETAIL: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/judge/${itemId}`,
  POST_CRAS_MANUAL_JUDGE_ADD: (siteId: string | number): string => DEAFULT_URL + `/api/v1/rule/cras/${siteId}/judge`,
  PUT_CRAS_MANUAL_JUDGE_EDIT: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/judge/${itemId}`,
  DELETE_CRAS_MANUAL_JUDGE_DELETE: (siteId: string | number, itemId: string | number): string =>
    DEAFULT_URL + `/api/v1/rule/cras/${siteId}/judge/${itemId}`,
  GET_CRAS_MANUAL_CREATE_OPTION: DEAFULT_URL + '/api/v1/rule/cras/option/create',
  GET_CRAS_MANUAL_JUDGE_OPTION: DEAFULT_URL + '/api/v1/rule/cras/option/judge',
};

export enum USER_ROLE {
  STATUS = 'ROLE_STATUS',
  JOB = 'ROLE_JOB',
  CONFIGURE = 'ROLE_CONFIGURE',
  RULES = 'ROLE_RULES',
  ADDRESS = 'ROLE_ADDRESS',
  ACCOUNT = 'ROLE_ACCOUNT',
}

export enum USER_ROLE_NAME {
  STATUS = 'STATUS',
  JOB = 'JOB',
  CONFIGURE = 'CONFIGURE',
  RULES = 'RULES',
  ADDRESS = 'ADDRESS BOOK',
  ACCOUNT = 'ACCOUNT',
}

export enum ERROR_MESSAGE {
  DUPLICATE_USERNAME = 'duplicate username',
  INVALID_PASSWORD = 'invalid password',
  INVALID_USERNAME = 'invalid username',
  INVALID_CURRENT_PASSWORD = 'invalid current password',
  INVALID_ROLES = 'invalid roles',
  INVALID_USER = 'invalid user',
}
