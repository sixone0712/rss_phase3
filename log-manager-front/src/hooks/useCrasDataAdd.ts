import { useForm } from 'antd/lib/form/Form';
import { AxiosError } from 'axios';
import { useCallback, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import { getCrasSiteInfoList, postCrasAddSite } from '../lib/api/axios/requests';
import { MUTATION_KEY } from '../lib/api/query/mutationKey';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { openNotification } from '../lib/util/notification';
import { crasShowAddSelector, setCrasAddVisibleReducer } from '../reducers/slices/crasData';
import { CrasDataSiteInfo } from '../types/crasData';

export interface FormCrasSiteName {
  siteId: number;
}

export default function useCrasDataAdd() {
  const [form] = useForm<FormCrasSiteName>();
  const queryClient = useQueryClient();
  const dispatch = useDispatch();
  const visible = useSelector(crasShowAddSelector);
  const setVisible = useCallback(
    (visible: boolean) => {
      dispatch(setCrasAddVisibleReducer(visible));
    },
    [dispatch]
  );

  const { data, isFetching: isFetchingNames } = useQuery<CrasDataSiteInfo[], AxiosError>(
    QUERY_KEY.RULES_CRAS_GET_SITE_INFO_LIST,
    () => getCrasSiteInfoList(),
    {
      initialData: [],
      placeholderData: [],
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of user-fab name`, error);
      },
    }
  );

  const { mutate, isLoading: isFetchingAdd } = useMutation((reqData: FormCrasSiteName) => postCrasAddSite(reqData), {
    mutationKey: MUTATION_KEY.RULES_CRAS_ADD_SITE,
    onError: (error: AxiosError) => {},
    onSuccess: () => {
      setVisible(false);
    },
  });

  const handleOk = useCallback(
    (reqData: FormCrasSiteName) => {
      mutate(reqData);
    },
    [mutate]
  );

  const handleCancel = () => {
    setVisible(false);
  };

  useEffect(() => {
    if (visible) {
      form.resetFields();
    }
  }, [visible]);

  return {
    form,
    visible,
    setVisible,
    data,
    isFetchingNames,
    isFetchingAdd,
    handleOk,
    handleCancel,
  };
}
