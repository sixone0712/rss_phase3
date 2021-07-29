import { Modal } from 'antd';
import { AxiosError } from 'axios';
import { useCallback, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { DeleteCrasDeleteSite, getCrasInfoList } from '../lib/api/axios/requests';
import { MUTATION_KEY } from '../lib/api/query/mutationKey';
import { QUERY_KEY } from '../lib/api/query/queryKey';
import { PAGE_URL } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import { initCrasReducer, setCrasAddVisibleReducer } from '../reducers/slices/crasData';
import { CrasDataInfo } from '../types/crasData';

export default function useCrasDataList() {
  const history = useHistory();
  const queryClient = useQueryClient();
  const dispatch = useDispatch();
  const { data: list, isFetching: isFetching, refetch: refetchList } = useQuery<CrasDataInfo[], AxiosError>(
    QUERY_KEY.RULES_CRAS_GET_INFO_LIST,
    () => getCrasInfoList(),
    {
      initialData: [],
      placeholderData: [],
      refetchOnWindowFocus: false,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to response the list of cras data!`, error);
      },
    }
  );

  const { mutateAsync: deleteMutateAsync, isLoading: isDeleting } = useMutation(
    (siteId: number) => DeleteCrasDeleteSite(siteId),
    {
      mutationKey: MUTATION_KEY.RULES_CRAS_DELETE,
      onError: (error: AxiosError) => {
        openNotification('error', 'Error', `Failed to delete cras data!`, error);
      },
      onSuccess: () => {
        openNotification('success', 'Success', 'Succeed to delete cras data');
      },
      onSettled: () => {
        refetchList();
      },
    }
  );

  const openDeleteModal = useCallback(
    (siteId: number) => {
      const confirm = Modal.confirm({
        className: 'edit-cras-data',
        title: 'Delete Cras Data',
        content: 'Are you sure to delete cras data?',
        onOk: async () => {
          await deleteMutateAsync(siteId);
        },
      });

      const diableCancelBtn = () => {
        confirm.update({
          cancelButtonProps: {
            disabled: true,
          },
        });
      };
    },
    [deleteMutateAsync]
  );

  // TODO
  const openImportModal = (siteId: number) => {};
  // TODO
  const openExportModal = (siteId: number) => {};

  const openEditModal = useCallback(
    (type: 'create' | 'judge', siteId: number, siteName: string) => {
      if (type === 'create') {
        history.push(`${PAGE_URL.RULES_CRAS_DATA_EDIT_CREATE(siteId, siteName)}`);
      } else {
        history.push(`${PAGE_URL.RULES_CRAS_DATA_EDIT_JUDGE(siteId, siteName)}`);
      }
    },
    [history]
  );

  const openAddModal = () => {
    dispatch(setCrasAddVisibleReducer(true));
  };
  const refreshStatusList = () => {
    queryClient.fetchQuery(QUERY_KEY.RULES_CRAS_GET_INFO_LIST);
  };

  useEffect(() => {
    dispatch(initCrasReducer());
  }, []);

  return {
    list,
    openDeleteModal,
    openImportModal,
    openExportModal,
    openEditModal,
    openAddModal,
    isFetching,
    refreshStatusList,
  };
}
