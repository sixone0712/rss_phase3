import { useForm } from 'antd/lib/form/Form';
import { SelectValue } from 'antd/lib/select';
import { PresetStatusColorType } from 'antd/lib/_util/colors';
import { AxiosError } from 'axios';
import { type } from 'node:os';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import {
  getCrasManualCreateInfoDetail,
  getCrasManualCreateTargetColumn,
  getCrasManualCreateTargetTable,
  postCrasManualCreateAdd,
  postCrasManualCreateTestQuery,
  putCrasManualCreateEdit,
} from '../lib/api/axios/requests';
import { ReqPostCrasDataCreateAdd, ReqPostCrasDataTestQuery, ReqPutCrasDataCreateEdit } from '../lib/api/axios/types';
import { MUTATION_KEY } from '../lib/api/query/mutationKey';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import {
  crasCreateOptionSelector,
  crasCreateTableSelector,
  crasDrawerTypeSelector,
  crasItemIdSelector,
  crasShowCreateDrawerSelector,
  crasSiteIdSelector,
  setCrasCreateColumnTableOption,
  setCrasCreateSelectTable,
  setCrasCreateTargetTableOption,
  setCrasShowCreateDrawerReducer,
} from '../reducers/slices/crasData';
import { CrasDataCreateInfo } from '../types/crasData';

export interface FormCrasDataCreateInfo extends Omit<CrasDataCreateInfo, 'itemId'> {}

export interface TestQueryStatusInfo {
  status: PresetStatusColorType;
  error: string;
}
export default function useCrasDataEditCreate() {
  const queryClient = useQueryClient();
  const [form] = useForm<FormCrasDataCreateInfo>();
  const dispatch = useDispatch();
  const isDrawer = useSelector(crasShowCreateDrawerSelector);
  const drawerType = useSelector(crasDrawerTypeSelector);
  const selectSiteId = useSelector(crasSiteIdSelector);
  const selectItemId = useSelector(crasItemIdSelector);
  const selectTable = useSelector(crasCreateTableSelector);
  const createOptions = useSelector(crasCreateOptionSelector);
  const [testQueryStatus, setTestQueryStatus] = useState<TestQueryStatusInfo>({
    status: 'default',
    error: '',
  });

  const { data: itemInfo, isFetching: isFetchingItems } = useQuery<CrasDataCreateInfo, AxiosError>(
    [QUERY_KEY.RULES_CRAS_GET_MANUAL_CREATE_DETAIL, selectSiteId, selectItemId],
    () => getCrasManualCreateInfoDetail(selectSiteId as number, selectItemId as number),

    {
      enabled: drawerType === 'edit' && isDrawer && Boolean(selectSiteId) && Boolean(selectItemId),
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of create cras data item`, error);
      },
      onSuccess: (resData) => {
        form.setFieldsValue({
          ...resData,
        });
        dispatch(setCrasCreateSelectTable(resData.targetTable));
      },
    }
  );

  const { data: targetTableList, isFetching: isFetchingTargetTable } = useQuery<string[], AxiosError>(
    [QUERY_KEY.RULES_CRAS_GET_MANUAL_CREATE_TARGET_TABLE_LIST, selectSiteId],
    () => getCrasManualCreateTargetTable(selectSiteId as number),
    {
      enabled: isDrawer && Boolean(selectSiteId),
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of target table!`, error);
        dispatch(setCrasCreateTargetTableOption([]));
      },
      onSuccess: (resData) => {
        dispatch(setCrasCreateTargetTableOption(resData));
      },
    }
  );

  const { data: columnTableList, isFetching: isFetchingTargetColumn } = useQuery<string[], AxiosError>(
    [QUERY_KEY.RULES_CRAS_GET_MANUAL_CREATE_TARGET_COLUMN_LIST, selectSiteId, selectTable],
    () => getCrasManualCreateTargetColumn(selectSiteId as number, selectTable as string),
    {
      enabled: isDrawer && Boolean(selectSiteId) && Boolean(selectTable),
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of target table!`, error);
        dispatch(setCrasCreateColumnTableOption([]));
      },
      onSuccess: (resData) => {
        dispatch(setCrasCreateColumnTableOption(resData));
      },
    }
  );

  const { mutate: testQueryMutate, isLoading: isTestQuerying } = useMutation(
    (postData: ReqPostCrasDataTestQuery) => postCrasManualCreateTestQuery(postData),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_CREATE_TEST_QUERY,
      onError: (error: AxiosError) => {
        setTestQueryStatus({ status: 'error', error: error.message });
      },
      onSuccess: () => {
        setTestQueryStatus({ status: 'success', error: '' });
      },
    }
  );

  const { mutate: addMutate, isLoading: isAdding } = useMutation(
    ({ siteId, postData }: { siteId: number; postData: ReqPostCrasDataCreateAdd }) =>
      postCrasManualCreateAdd(siteId, postData),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_CREATE_ADD,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to add cras create data!`, error);
      },
      onSuccess: () => {
        openNotification('success', 'Success', 'Succeed to add cras create data.');
      },
      onSettled: () => {
        queryClient.fetchQuery([QUERY_KEY.RULES_CRAS_GET_MANUAL_CREATE, selectSiteId]);
        dispatch(setCrasShowCreateDrawerReducer(false));
      },
    }
  );
  const { mutate: editMutate, isLoading: isEditing } = useMutation(
    ({ siteId, itemId, postData }: { siteId: number; itemId: number; postData: ReqPutCrasDataCreateEdit }) =>
      putCrasManualCreateEdit(siteId, itemId, postData),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_CREATE_EDIT,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to edit cras create data!`, error);
      },
      onSuccess: () => {
        openNotification('success', 'Success', 'Succeed to edit create cras data.');
      },
      onSettled: () => {
        queryClient.fetchQuery([QUERY_KEY.RULES_CRAS_GET_MANUAL_CREATE, selectSiteId]);
        dispatch(setCrasShowCreateDrawerReducer(false));
      },
    }
  );

  const isDisableItems = useMemo(
    () => isFetchingItems || isTestQuerying || isAdding || isEditing || isFetchingTargetTable || isFetchingTargetColumn,

    [isFetchingItems, isTestQuerying, isAdding, isEditing, isFetchingTargetTable, isFetchingTargetColumn]
  );

  const closeDrawer = () => {
    dispatch(setCrasShowCreateDrawerReducer(false));
  };

  const onSelectTargetTable = useCallback((value: SelectValue) => {
    dispatch(setCrasCreateSelectTable(value as string));
  }, []);

  const onFinish = useCallback(() => {
    const reqData = form.getFieldsValue();
    if (drawerType === 'add') {
      selectSiteId && addMutate({ siteId: selectSiteId, postData: reqData });
    } else {
      selectSiteId && selectItemId && editMutate({ siteId: selectSiteId, itemId: selectItemId, postData: reqData });
    }
  }, [drawerType, selectSiteId, selectItemId, addMutate, editMutate]);

  const testQuery = async () => {
    try {
      const { manualWhere, targetCol, targetTable } = await form.validateFields([
        'targetTable',
        'targetCol',
        'manualWhere',
      ]);
      setTestQueryStatus({ status: 'processing', error: '' });
      testQueryMutate({ siteId: selectItemId as number, manualWhere, targetCol, targetTable });
    } catch (e) {
      console.error(e);
      setTestQueryStatus({ status: 'default', error: '' });
    }
  };

  const initData = useCallback(() => {
    if (drawerType === 'add') form.resetFields();
    setTestQueryStatus({ status: 'default', error: '' });
  }, [drawerType, form]);

  useEffect(() => {
    if (isDrawer) {
      initData();
    }
  }, [isDrawer]);

  return {
    form,
    drawerType,
    isDrawer,
    closeDrawer,
    onFinish,
    initData,
    createOptions,
    onSelectTargetTable,
    isDisableItems,
    isFetchingItems,
    isTestQuerying,
    isAdding,
    isEditing,
    isFetchingTargetTable,
    isFetchingTargetColumn,
    selectTable,
    testQuery,
    testQueryStatus,
  };
}
