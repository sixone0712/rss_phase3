import { useForm } from 'antd/lib/form/Form';
import { PresetStatusColorType } from 'antd/lib/_util/colors';
import { AxiosError } from 'axios';
import { useCallback, useEffect, useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import {
  getCrasManualJudgeInfoDetail,
  postCrasManualJudgeAdd,
  putCrasManualJudgeEdit,
} from '../lib/api/axios/requests';
import { ReqPostCrasDataJudgeAdd, ReqPutCrasDataJudgeEdit } from '../lib/api/axios/types';
import { MUTATION_KEY } from '../lib/api/query/mutationKey';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import {
  crasDrawerTypeSelector,
  crasItemIdSelector,
  crasJudgeOptionSelector,
  crasShowJudgeDrawerSelector,
  crasSiteIdSelector,
  setCrasShowJudgeDrawerReducer,
} from '../reducers/slices/crasData';
import { CrasDataJudgeInfo } from '../types/crasData';

export interface FormCrasDataJudgeInfo extends Omit<CrasDataJudgeInfo, 'itemId'> {}

export interface TestQueryStatusInfo {
  status: PresetStatusColorType;
  error: string;
}
export default function useCrasDataEditJudge() {
  const queryClient = useQueryClient();
  const [form] = useForm<FormCrasDataJudgeInfo>();
  const dispatch = useDispatch();
  const isDrawer = useSelector(crasShowJudgeDrawerSelector);
  const drawerType = useSelector(crasDrawerTypeSelector);
  const selectSiteId = useSelector(crasSiteIdSelector);
  const selectItemId = useSelector(crasItemIdSelector);
  const judgeOptions = useSelector(crasJudgeOptionSelector);

  const { data: itemInfo, isFetching: isFetchingItems } = useQuery<CrasDataJudgeInfo, AxiosError>(
    [QUERY_KEY.RULES_CRAS_GET_MANUAL_JUDGE_DETAIL, selectSiteId, selectItemId],
    () => getCrasManualJudgeInfoDetail(selectSiteId as number, selectItemId as number),

    {
      enabled: drawerType === 'edit' && isDrawer && Boolean(selectSiteId) && Boolean(selectItemId),
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of cras data judge rules item`, error);
      },
      onSuccess: (resData) => {
        form.setFieldsValue({
          ...resData,
        });
      },
    }
  );

  const { mutate: addMutate, isLoading: isAdding } = useMutation(
    ({ siteId, postData }: { siteId: number; postData: ReqPostCrasDataJudgeAdd }) =>
      postCrasManualJudgeAdd(siteId, postData),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_JUDGE_ADD,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to add cras data judge rules item!`, error);
      },
      onSuccess: () => {
        openNotification('success', 'Success', 'Succeed to add cras data judge rules item.');
      },
      onSettled: () => {
        queryClient.fetchQuery([QUERY_KEY.RULES_CRAS_GET_MANUAL_JUDGE, selectSiteId]);
        dispatch(setCrasShowJudgeDrawerReducer(false));
      },
    }
  );
  const { mutate: editMutate, isLoading: isEditing } = useMutation(
    ({ siteId, itemId, postData }: { siteId: number; itemId: number; postData: ReqPutCrasDataJudgeEdit }) =>
      putCrasManualJudgeEdit(siteId, itemId, postData),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_JUDGE_EDIT,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to edit cras data judge rules item!`, error);
      },
      onSuccess: () => {
        openNotification('success', 'Success', 'Succeed to edit cras data judge rules item.');
      },
      onSettled: () => {
        queryClient.fetchQuery([QUERY_KEY.RULES_CRAS_GET_MANUAL_JUDGE, selectSiteId]);
        dispatch(setCrasShowJudgeDrawerReducer(false));
      },
    }
  );

  const isDisableItems = useMemo(
    () => isFetchingItems || isAdding || isEditing,

    [isFetchingItems, isAdding, isEditing]
  );

  const closeDrawer = () => {
    dispatch(setCrasShowJudgeDrawerReducer(false));
  };

  const onFinish = useCallback(() => {
    const reqData = form.getFieldsValue();
    if (drawerType === 'add') {
      selectSiteId && addMutate({ siteId: selectSiteId, postData: reqData });
    } else {
      selectSiteId && selectItemId && editMutate({ siteId: selectSiteId, itemId: selectItemId, postData: reqData });
    }
  }, [drawerType, selectSiteId, selectItemId, addMutate, editMutate]);

  const initData = useCallback(() => {
    if (drawerType === 'add') form.resetFields();
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
    judgeOptions,
    isDisableItems,
    isFetchingItems,
    isAdding,
    isEditing,
  };
}
