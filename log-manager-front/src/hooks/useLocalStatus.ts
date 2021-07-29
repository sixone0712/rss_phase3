import { Modal } from 'antd';
import { useCallback } from 'react';
import { useQuery, useQueryClient } from 'react-query';
import { deleteLocalJob, getLocalJobStatus } from '../lib/api/axios/requests';
import { openNotification } from '../lib/util/notification';
import { LocalStatus } from '../types/status';

export default function useLocalStatus() {
  const queryClient = useQueryClient();
  const { data: localList, isFetching, isError } = useQuery<LocalStatus[]>('get_status_local', getLocalJobStatus, {
    initialData: [],
    refetchOnWindowFocus: false,
    onError: () => {
      openNotification('error', 'Error', 'Failed to response local status list');
      queryClient.setQueryData('get_status_local', []);
    },
  });

  const refreshRemoteList = useCallback(() => {
    queryClient.fetchQuery('get_status_local');
  }, [queryClient]);

  const openDeleteModal = useCallback(
    (jobId: number) => {
      const confirm = Modal.confirm({
        className: 'delete-local-job',
        title: 'Delete Local Job',
        content: 'Are you sure to delete local job?',
        onOk: async () => {
          diableCancelBtn();
          try {
            await deleteLocalJob(jobId);
            openNotification('success', 'Success', 'Succeed to delete local job.');
          } catch (e) {
            openNotification('error', 'Error', 'Failed to delete local job!');
          } finally {
            refreshRemoteList();
          }
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
    [refreshRemoteList]
  );

  return {
    localList,
    isFetching,
    isError,
    refreshRemoteList,
    openDeleteModal,
  };
}
