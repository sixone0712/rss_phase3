import { Modal } from 'antd';
import { useCallback } from 'react';
import { useQuery, useQueryClient } from 'react-query';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router';
import {
  deleteRemoteJob,
  getRemoteJobStatus,
  getRemoteJobStopStatus,
  startRemoteJob,
  stopRemoteJob,
} from '../lib/api/axios/requests';
import { PAGE_URL } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import { LoginUserSelector } from '../reducers/slices/loginUser';
import { RemoteJobStatus, StatusStepType } from '../types/status';

export default function useRemoteStatus() {
  const { data: remoteList, isFetching } = useQuery<RemoteJobStatus[]>('get_status_remote', getRemoteJobStatus, {
    initialData: [],
    refetchOnWindowFocus: false,
    onError: () => {
      openNotification('error', 'Error', `Failed to response the status of remote`);
    },
  });
  const history = useHistory();
  const queryClient = useQueryClient();
  const loggedInUser = useSelector(LoginUserSelector);

  const moveToRemoteNewJob = useCallback(() => {
    history.push(PAGE_URL.STATUS_REMOTE_ADD);
  }, []);

  const moveToRemoteEditJob = useCallback((jobid: number, siteId: number, siteName: string) => {
    history.push(PAGE_URL.STATUS_REMOTE_EDIT({ jobid, siteId, siteName }));
  }, []);

  const moveToRemoteHistory = useCallback((id: number, siteName: string, type: StatusStepType) => {
    history.push(PAGE_URL.STATUS_REMOTE_BUILD_HISTORY({ id, type, siteName }));
  }, []);

  const refreshRemoteList = useCallback(() => {
    queryClient.fetchQuery('get_status_remote');
  }, [queryClient]);

  const openStartStopModal = useCallback(
    ({
      action,
      jobId,
      siteId,
      companyFabName,
      prevStop,
    }: {
      action: 'start' | 'stop';
      jobId: number;
      siteId: number;
      companyFabName: string;
      prevStop: boolean;
    }) => {
      const actionText = action === 'start' ? 'Start' : 'End';
      const confirm = Modal.confirm({
        className: `${action}_remote_job`,
        title: `${actionText} Remote Job`,
        content: `Are you sure to ${action} remote job '${companyFabName}'?`,
        onOk: async () => {
          diableCancelBtn();
          try {
            const { stop } = await getRemoteJobStopStatus(jobId);
            if (prevStop !== stop) {
              openNotification(
                'error',
                'Error',
                `The information of remote job '${companyFabName}' on the server has been changed. So, run the update. please try again!`
              );
              refreshRemoteList();
            } else {
              if (action === 'start') await startRemoteJob(jobId);
              else await stopRemoteJob(jobId);
              openNotification('success', 'Success', `Succeed to ${action} remote job '${companyFabName}'.`);
            }
          } catch (e) {
            openNotification('error', 'Error', `Failed to ${action} remote job '${companyFabName}'!`);
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

  const openDeleteModal = useCallback(
    ({
      jobId,
      siteId,
      companyFabName,
      prevStop,
    }: {
      jobId: number;
      siteId: number;
      companyFabName: string;
      prevStop: boolean;
    }) => {
      const confirm = Modal.confirm({
        className: 'delete_remote_job',
        title: 'Delete Remote Job',
        content: `Are you sure to delete remote job '${companyFabName}'?`,
        onOk: async () => {
          diableCancelBtn();
          try {
            const { stop } = await getRemoteJobStopStatus(jobId);

            if (prevStop !== stop) {
              openNotification(
                'error',
                'Error',
                `The information of remote job '${companyFabName}' on the server has been changed. So, run the update. please try again!`
              );
            } else {
              if (stop) {
                await deleteRemoteJob(jobId);
                openNotification('success', 'Success', `Succeed to delete remote job '${companyFabName}'.`);
              } else {
                openNotification('error', 'Error', `After stop remote job '${companyFabName}', please try again!`);
              }
            }
          } catch (e) {
            openNotification('error', 'Error', `Failed to delete remote job '${companyFabName}'!`);
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

  const openEditeModal = useCallback(
    ({
      jobId,
      siteId,
      companyFabName,
      prevStop,
    }: {
      jobId: number;
      siteId: number;
      companyFabName: string;
      prevStop: boolean;
    }) => {
      const confirm = Modal.confirm({
        className: 'edit_remote_job',
        title: 'Edit Remote Job',
        content: `Are you sure to edit remote job '${companyFabName}'?`,
        onOk: async () => {
          diableCancelBtn();
          try {
            const { stop } = await getRemoteJobStopStatus(jobId);
            if (prevStop !== stop) {
              openNotification(
                'error',
                'Error',
                `The information of remote job '${companyFabName}' on the server has been changed. So, run the update. please try again!`
              );
            } else {
              if (stop) {
                moveToRemoteEditJob(jobId, siteId, companyFabName);
              } else {
                openNotification('error', 'Error', `After Stop remote job '${companyFabName}', please try again!`);
              }
            }
          } catch (e) {
            openNotification('error', 'Error', `Failed to edit remote job '${companyFabName}'!`);
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
    [moveToRemoteEditJob, refreshRemoteList]
  );

  return {
    remoteList,
    isFetching,
    refreshRemoteList,
    moveToRemoteNewJob,
    moveToRemoteEditJob,
    moveToRemoteHistory,
    openDeleteModal,
    openStartStopModal,
    openEditeModal,
    loggedInUser,
  };
}
