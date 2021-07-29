import { FormCrasSiteName } from '../../../hooks/useCrasDataAdd';
import { DEFAULT_ALL_ADDRESS_KEY } from '../../../reducers/slices/address';
import {
  CrasDataCreateInfo,
  CrasDataCreateOption,
  CrasDataInfo,
  CrasDataJudgeInfo,
  CrasDataJudgeOption,
  CrasDataManualInfo,
  CrasDataSiteInfo,
} from '../../../types/crasData';
import { LocalStatus, RemoteJobStatus, RemotePlan } from '../../../types/status';
import { API_URL } from '../../constants';
import { rolesToBoolean } from '../../util/convertUserRoles';
import client from './client';
import {
  AddressInfo,
  LoginUserInfo,
  ReqGetBuildHistoryList,
  ReqGetSiteDBInfo,
  ReqLogin,
  ReqPostAddEmail,
  ReqPostAddGroup,
  ReqPostCrasConnection,
  ReqPostCrasDataCreateAdd,
  ReqPostCrasDataJudgeAdd,
  ReqPostCrasDataTestQuery,
  ReqPostEmailConnection,
  ReqPostHostDBInfo,
  ReqPostLocalJob,
  ReqPostRemoteJob,
  ReqPostRssConnection,
  ReqPostSiteDBInfo,
  ReqPutCrasDataCreateEdit,
  ReqPutCrasDataJudgeEdit,
  ReqPutEditEmail,
  ReqPutEditGroup,
  ReqPutRemoteJob,
  ReqPutSiteDBInfo,
  ReqUser,
  ReqUserPassword,
  ReqUserRoles,
  ResGetAddressInfo,
  ResGetBuildHistoryList,
  ResGetHostDBInfo,
  ResGetLocalJobStatus,
  ResGetLoginInfo,
  ResGetLogMonitorVersion,
  ResGetRemoteJob,
  ResGetRemoteJobStatus,
  ResGetRemotePlan,
  ResGetSiteJobStatus,
  ResGetSiteName,
  ResPostLocalJob,
  ResPostRemoteJob,
  ResPutRemoteJob,
  ResUser,
  SiteDBInfo,
  UserInfo,
} from './types';

export const getConfigureSitesNames = async (): Promise<ResGetSiteName[]> => {
  const { data } = await client.get<ResGetSiteName[]>(API_URL.GET_CONFIGURE_SITE_NAME);
  return data.map((item, index) => ({
    ...item,
    crasCompanyFabName: `${item.crasCompanyName}-${item.crasFabName}`,
  }));
};

export const getConfigureSitesNamesNotAdded = async (): Promise<ResGetSiteName[]> => {
  const { data } = await client.get<ResGetSiteName[]>(API_URL.GET_CONFIGURE_SITE_NAME_NOT_ADDED);
  return data.map((item, index) => ({
    ...item,
    crasCompanyFabName: `${item.crasCompanyName}-${item.crasFabName}`,
  }));
};

export const getRemoteJobStatus = async (): Promise<RemoteJobStatus[]> => {
  const { data } = await client.get<ResGetRemoteJobStatus[]>(API_URL.GET_STATUS_REMOTE_JOB_LIST);

  return data.map((item, index) => ({
    ...item,
    index: index,
    companyFabName: `${item.companyName}-${item.fabName}`,
  }));
};

export const getRemoteJob = async (id: number | string): Promise<ResGetRemoteJob> => {
  const { data } = await client.get<ResGetRemoteJob>(API_URL.GET_STATUS_REMOTE_JOB_DETAIL(id));
  return data;
};

export const getRemoteJobStopStatus = async (jobId: number | string): Promise<{ stop: boolean }> => {
  const { data } = await client.get<{ stop: boolean }>(API_URL.GET_STATUS_REMOTE_JOB_STOP_STATUS(jobId));
  return data;
};

export const getRemotePlans = async (siteId: string | undefined): Promise<RemotePlan[]> => {
  const { data } = await client.get<ResGetRemotePlan[]>(API_URL.GET_STATUS_REMOTE_PLAN_LIST(siteId!));
  return data.map((item, index) => ({
    ...item,
    index: index,
    machineCount: item.machineNames.length,
    targetCount: item.targetNames.length,
  }));
};

export const getLocalJobStatus = async (): Promise<LocalStatus[]> => {
  const { data } = await client.get<ResGetLocalJobStatus[]>(API_URL.GET_STATUS_LOCAL_JOB_LIST);
  return data.map((item, index) => ({
    ...item,
    index: index,
    companyFabName: `${item.companyName}-${item.fabName}`,
    files: item.fileOriginalNames.length,
  }));
};

export const postLocalJob = async (reqData: ReqPostLocalJob) => {
  const { data } = await client.post<ResPostLocalJob>(API_URL.POST_STATUS_LOCAL_JOB, reqData);
  return data;
};

export const deleteLocalJob = async (jobId: number) => {
  const { data } = await client.delete(API_URL.DELETE_STATUS_LOCAL_JOB(jobId));
  return data;
};

export const postRemoteJob = async (reqData: ReqPostRemoteJob) => {
  const { data } = await client.post<ResPostRemoteJob>(API_URL.POST_STATUS_REMOTE_JOB, reqData);
  return data;
};

export const putRemoteJob = async (reqData: ReqPutRemoteJob) => {
  const { data } = await client.put<ResPutRemoteJob>(API_URL.PUT_STATUS_REMOTE_JOB(reqData.jobId), reqData.data);
  return data;
};

export const deleteRemoteJob = async (jobId: string | number) => {
  const { data } = await client.delete(API_URL.DELETE_STATUS_REMOTE_JOB(jobId));
  return data;
};

export const stopRemoteJob = async (jobId: string | number) => {
  const { data } = await client.patch(API_URL.STOP_STATUS_REMOTE_JOB(jobId));
  return data;
};

export const startRemoteJob = async (jobId: string | number) => {
  const { data } = await client.patch(API_URL.RUN_STATUS_REMOTE_JOB(jobId));
  return data;
};

export const getHostDBInfo = async (): Promise<ResGetHostDBInfo> => {
  const { data } = await client.get<ResGetHostDBInfo>(API_URL.GET_CONFIGURE_HOST_DB);
  return data;
};

export const postHostDBInfo = async (reqData: ReqPostHostDBInfo) => {
  const { data } = await client.post(API_URL.POST_CONFIGURE_HOST_DB, reqData);
  return data;
};

export const getSiteDBInfo = async (): Promise<SiteDBInfo[]> => {
  const { data } = await client.get<ReqGetSiteDBInfo[]>(API_URL.GET_CONFIGURE_SITE_DB);
  return data.map((item, index) => ({
    ...item,
    index: index,
    crasCompanyFabName: `${item.crasCompanyName}-${item.crasFabName}`,
  }));
};

export const postSiteDBInfo = async (reqData: ReqPostSiteDBInfo) => {
  const { data } = await client.post(API_URL.POST_CONFIGURE_SITE_DB, reqData);
  return data;
};

export const putSiteDBInfo = async (reqData: ReqPutSiteDBInfo) => {
  const { data } = await client.put(API_URL.PUT_CONFIGURE_SITE_DB(reqData.siteId), reqData);
  return data;
};

export const deleteSiteDBInfo = async (siteId: string | number) => {
  const { data } = await client.delete(API_URL.DELETE_CONFIGURE_SITE_DB(siteId));
  return data;
};

export const postCrasConnection = async (reqData: ReqPostCrasConnection) => {
  const { data } = await client.post(API_URL.GET_CONFITURE_CRAS_CONNECTION, reqData);
  return data;
};

export const postEmailConnection = async (reqData: ReqPostEmailConnection) => {
  const { data } = await client.post(API_URL.GET_CONFITURE_EMAIL_CONNECTION, reqData);
  return data;
};
export const postRssConnection = async (reqData: ReqPostRssConnection) => {
  const { data } = await client.post(API_URL.GET_CONFITURE_RSS_CONNECTION, reqData);
  return data;
};

export const getHistoryBuildList = async (reqData: ReqGetBuildHistoryList): Promise<ResGetBuildHistoryList[]> => {
  const { data } = await client.get(API_URL.GET_STATUS_BUILD_HISTORY_LIST(reqData));
  return data;
};

export const getLogMonitorServerVersion = async (): Promise<ResGetLogMonitorVersion> => {
  const { data } = await client.get(API_URL.GET_CONFIGURE_LOG_MONITOR_VERSION);
  return data;
};

export const getSiteJobStatus = async (siteId: string | number): Promise<ResGetSiteJobStatus> => {
  const { data } = await client.get(API_URL.GET_CONFIGURE_SITE_JOB_STATUS(siteId));
  return data;
};

export const getLogMonitorOos = async (): Promise<string> => {
  const { data } = await client.get(API_URL.GET_LOG_MONITOR_OOS);
  return data;
};

export const login = async (loginData: ReqLogin): Promise<LoginUserInfo> => {
  const { data } = await client.get<ResGetLoginInfo>(API_URL.GET_AUTH_LOGIN(loginData.username, loginData.password));

  return {
    id: data.id,
    username: data.username,
    roles: {
      ...rolesToBoolean(data.roles),
    },
  };
};

export const getMe = async (): Promise<LoginUserInfo> => {
  const { data } = await client.get<ResGetLoginInfo>(API_URL.GET_AUTH_ME);

  return {
    id: data.id,
    username: data.username,
    roles: {
      ...rolesToBoolean(data.roles),
    },
  };
};

export const logout = async () => {
  const { data } = await client.get(API_URL.GET_AUTH_LOGOUT);
  return data;
};

export const getUsers = async (): Promise<UserInfo[]> => {
  const { data } = await client.get<ResUser[]>(API_URL.GET_USER_LIST);
  return data.map((item, index) => ({
    index: index,
    ...item,
    roles: {
      ...rolesToBoolean(item.roles),
    },
  }));
};

export const postUser = async (reqData: ReqUser) => {
  const { data } = await client.post(API_URL.POST_USER_SIGN_UP, reqData);
  return data;
};

export const putUserPassword = async (userId: string | number, reqData: ReqUserPassword) => {
  const { data } = await client.put(API_URL.PUT_USER_PASSWORD(userId), reqData);
  return data;
};

export const putUserRoles = async (userId: string | number, reqData: ReqUserRoles) => {
  const { data } = await client.put(API_URL.PUT_USER_ROLES(userId), reqData);
  return data;
};

export const getAddressGroupList = async (): Promise<AddressInfo[]> => {
  const { data } = await client.get<ResGetAddressInfo[]>(API_URL.GET_ADDRESS_GROUP_LIST);
  // return data.map((res, idx) => ({
  //   index: idx,
  //   ...res,
  // }));
  return data;
};

export const getAddressGroupListInEmail = async (emailId: number | string): Promise<AddressInfo[]> => {
  const { data } = await client.get<ResGetAddressInfo[]>(API_URL.GET_ADDRESS_GROUP_LIST_IN_EMAIL(emailId));
  // return data.map((res, idx) => ({
  //   index: idx,
  //   ...res,
  // }));
  return data;
};

export const getAddressEmailList = async (gorupId: string | number | undefined): Promise<AddressInfo[]> => {
  if (!gorupId || +gorupId === DEFAULT_ALL_ADDRESS_KEY) {
    const { data } = await client.get<ResGetAddressInfo[]>(API_URL.GET_ADDRESS_EMAIL_LIST);
    // return data.map((res, idx) => ({
    //   index: idx,
    //   ...res,
    // }));
    return data;
  } else {
    const { data } = await client.get<ResGetAddressInfo[]>(API_URL.GET_ADDRESS_EMAIL_LIST_BY_GROUP(gorupId));
    // return data.map((res, idx) => ({
    //   index: idx,
    //   ...res,
    // }));
    return data;
  }
};

export const deleteAddressEmail = async (emailIds: number[]) => {
  const { data } = await client.delete(API_URL.DELETE_ADDRESS_DELETE_EMAIL(emailIds));
  return data;
};

export const searchAddressEmail = async (keyword: string): Promise<AddressInfo[]> => {
  const { data } = await client.get<ResGetAddressInfo[]>(API_URL.SEARCH_ADDRESS_EMAIL(keyword));
  // return data.map((res, idx) => ({
  //   index: idx,
  //   ...res,
  // }));
  return data;
};

export const searchAddressEmailAndGroup = async (keyword: string): Promise<AddressInfo[]> => {
  const { data } = await client.get<ResGetAddressInfo[]>(API_URL.SEARCH_ADDRESS_GROUP_EMAIL(keyword));
  // return data.map((res, idx) => ({
  //   index: idx,
  //   ...res,
  // }));
  return data;
};

export const postAddressAddEmail = async (reqData: ReqPostAddEmail) => {
  const { data } = await client.post(API_URL.POST_ADDRESS_ADD_EMAIL, reqData);
  return data;
};

export const putAddressEditEmail = async (reqData: ReqPutEditEmail) => {
  const { id, name, email, groupIds } = reqData;
  const { data } = await client.put(API_URL.PUT_ADDRESS_EDIT_EMAIL(id), { name, email, groupIds });
  return data;
};

export const postAddressAddGroup = async (reqData: ReqPostAddGroup) => {
  const { data } = await client.post(API_URL.POST_ADDRESS_ADD_GROUP, reqData);
  return data;
};

export const putAddressEditGroup = async (reqData: ReqPutEditGroup) => {
  const { id, name, emailIds } = reqData;
  const { data } = await client.put(API_URL.PUT_ADDRESS_EDIT_GROUP(id!), { name, emailIds });
  return data;
};

export const deleteAddressGroup = async (groupId: number | string) => {
  const { data } = await client.delete(API_URL.DELETE_ADDRESS_DELETE_GROUP(groupId));
  return data;
};

export const getAddressGroupEmailList = async (): Promise<AddressInfo[]> => {
  const { data } = await client.get<ResGetAddressInfo[]>(API_URL.GET_ADDRESS_GROUP_EMAIL_LIST);
  return data;
};

export const getCrasInfoList = async (): Promise<CrasDataInfo[]> => {
  // const { data } = await client.get<ResGetCrasDataList[]>(API_URL.GET_CRAS_LIST);
  // return data.map((item, idx) => ({
  //   index: idx,
  //   ...item,
  // }));

  return [
    {
      index: 0,
      siteId: 1,
      companyFabName: 'GKC-BQ',
      createCrasDataItemCount: 22,
      crasDataJudgeRulesItemCount: 20,
      date: '2021-07-01',
    },
    {
      index: 1,
      siteId: 2,
      companyFabName: 'GKC-BQ2',
      createCrasDataItemCount: 23,
      crasDataJudgeRulesItemCount: 24,
      date: '2021-07-02',
    },
  ];
};

export const getCrasSiteInfoList = async (): Promise<CrasDataSiteInfo[]> => {
  // const { data } = await client.get<CrasSiteName[]>(API_URL.GET_CRAS_SITE_NAME);
  // return data;
  return [
    {
      siteId: 1,
      name: 'GGG-GGG',
    },
    {
      siteId: 2,
      name: 'CCC_CCC',
    },
  ];
};

export const postCrasAddSite = async (reqData: FormCrasSiteName) => {
  // const { data } = await client.post(API_URL.POST_CRAS_SITE_ADD, reqData);
  // return data;
  return {};
};

export const DeleteCrasDeleteSite = async (siteId: number) => {
  // const { data } = await client.delete(API_URL.DELETE_CRAS_SITE_DELETE(siteId));
  // return data;
  return {};
};

export const getCrasManualCreateInfoList = async (siteId: number): Promise<CrasDataManualInfo[]> => {
  // const { data } = await client.get<ResGetCrasDataManualInfo[]>(API_URL.GET_CRAS_MANUAL_CREATE_INFO_LIST(siteId));
  // return data.map((item, idx) => ({
  //   index: idx,
  //   ...item,
  // }));

  return [
    {
      index: 0,
      itemId: 1,
      itemName: 'itemName1',
      enable: true,
    },
    {
      index: 1,
      itemId: 2,
      itemName: 'itemName2',
      enable: false,
    },
  ];
};

export const getCrasManualCreateInfoDetail = async (siteId: number, itemId: number): Promise<CrasDataCreateInfo> => {
  // const { data } = await client.get<CrasDataCreateInfo>(API_URL.GET_CRAS_MANUAL_CREATE_INFO_DETAIL(siteId, itemId));
  // return data;
  return {
    itemId: 1,
    itemName: 'itemName1',
    enable: true,
    targetTable: 'tableName',
    targetCol: ['colName1', 'colName2'],
    comments: 'comments',
    operations: 'max',
    calPeriodUnit: 'day',
    calResultType: 'integer',
    coef: 2,
    manualWhere: 'a>s',
  };
};

export const getCrasManualCreateTargetTable = async (siteId: number): Promise<string[]> => {
  // const { data } = await client.get<string[]>(API_URL.GET_CRAS_MANUAL_CREATE_TARGET_TABLE(siteId));
  // return data;
  return ['Table1', 'Table2', 'Table3', 'Table4'];
};

export const getCrasManualCreateTargetColumn = async (siteId: number, tableName: string): Promise<string[]> => {
  // const { data } = await client.get<string[]>(API_URL.GET_CRAS_MANUAL_CREATE_TARGET_COLUMN(siteId, tableName));
  // return data;
  return ['Column1', 'Column2', 'Column3', 'Column4'];
};

export const getCrasManualJudgeInfoList = async (siteId: number): Promise<CrasDataManualInfo[]> => {
  // const { data } = await client.get<ResGetCrasDataManualInfo[]>(API_URL.GET_CRAS_MANUAL_JUDGE_INFO_LIST(siteId));
  // return data.map((item, idx) => ({
  //   index: idx,
  //   ...item,
  // }));
  return [
    {
      index: 0,
      itemId: 1,
      itemName: 'itemName1',
      enable: true,
    },
    {
      index: 1,
      itemId: 2,
      itemName: 'itemName2',
      enable: false,
    },
  ];
};

export const getCrasManualJudgeInfoDetail = async (siteId: number, itemId: number): Promise<CrasDataJudgeInfo> => {
  // const { data } = await client.get<CrasDataJudgeInfo>(API_URL.GET_CRAS_MANUAL_JUDGE_INFO_DETAIL(siteId, itemId));
  // return data;
  return {
    itemId: 1,
    itemName: 'itemName1',
    title: 'titleInput',
    description: 'descriptionInput',
    calRange: 23,
    calCondition: 'sum',
    threshold: 1000,
    compare: 'onver',
    enable: true,
  };
};

export const postCrasManualCreateTestQuery = async (reqData: ReqPostCrasDataTestQuery) => {
  // const { data } = await client.post(API_URL.GET_CRAS_MANUAL_CREATE_TEST_QUERY, reqData);
  // return data;
  return {};
};

export const postCrasManualCreateAdd = async (siteId: number, reqData: ReqPostCrasDataCreateAdd) => {
  // const { data } = await client.post(API_URL.POST_CRAS_MANUAL_CREATE_ADD(siteId), reqData);
  // return data;
  return {};
};

export const putCrasManualCreateEdit = async (siteId: number, itemId: number, reqData: ReqPutCrasDataCreateEdit) => {
  // const { data } = await client.put(API_URL.POST_CRAS_MANUAL_CREATE_EDIT(siteId, itemId), reqData);
  // return data;
  return {};
};

export const deleteCrasManualCreateDelete = async (siteId: number, itemId: number) => {
  // const { data } = await client.delete(API_URL.DELETE_CRAS_MANUAL_CREATE_DELETE(siteId, itemId));
  // return data;
  return {};
};

export const postCrasManualJudgeAdd = async (siteId: number, reqData: ReqPostCrasDataJudgeAdd) => {
  // const { data } = await client.post(API_URL.POST_CRAS_MANUAL_JUDGE_ADD(siteId), reqData);
  // return data;
  return {};
};

export const putCrasManualJudgeEdit = async (siteId: number, itemId: number, reqData: ReqPutCrasDataJudgeEdit) => {
  // const { data } = await client.put(API_URL.POST_CRAS_MANUAL_JUDGE_EDIT(siteId, itemId), reqData);
  // return data;
  return {};
};

export const deleteCrasManualJudgeDelete = async (siteId: number, itemId: number) => {
  // const { data } = await client.delete(API_URL.DELETE_CRAS_MANUAL_JUDGE_DELETE(siteId, itemId));
  // return data;
  return {};
};

export const getCrasManualCreateOption = async (): Promise<CrasDataCreateOption> => {
  // const { data } = await client.put(API_URL.GET_CRAS_MANUAL_CREATE_OPTION);
  // return data;
  return {
    operations: [
      'max',
      'min',
      'stddev',
      'variance',
      'average',
      'range',
      'absmax',
      'absmin',
      'stddevp',
      'variancep',
      'free',
      'nop',
    ],
    calPeriodUnit: ['day', 'job', 'lot'],
    calResultType: ['integer', 'float', 'time', 'etc'],
  };
};

export const getCrasManualJudgeOption = async (): Promise<CrasDataJudgeOption> => {
  // const { data } = await client.put(API_URL.GET_CRAS_MANUAL_JUDGE_OPTION);
  // return data;
  return {
    condition: ['Sum', 'Ave', 'Diff', 'Rate', 'Range', 'Coef'],
    compare: ['Over', 'Under', 'AbsOver', 'AbsUnder'],
  };
};
