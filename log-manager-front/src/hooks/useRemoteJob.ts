import { Modal } from 'antd';
import { useCallback, useState } from 'react';
import { useQuery } from 'react-query';
import { useHistory } from 'react-router-dom';
import { REMOTE_STEP } from '../components/modules/RemoteJob/RemoteJob';
import { getRemoteJob, postRemoteJob, putRemoteJob } from '../lib/api/axios/requests';
import { ResGetRemoteJob } from '../lib/api/axios/types';
import { PAGE_URL } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import { RemoteJobType } from '../pages/Status/Remote/Remote';
import { RemoteJobState } from '../reducers/slices/remoteJob';
import useRemoteJobSlices from './useRemoteJobSlices';

enum REMOTE_ERROR {
  NO_ERROR = 0,
  NOT_SELECTED_SITE,
  NOT_SELECTED_PLANS,
  NOT_SELECTED_SENDING_TIME,
  NOT_ADD_ERROR_SUMMARY_TO,
  NOT_ADD_ERROR_SUMMARY_SUBJECT,
  NOT_ADD_ERROR_SUMMARY_CONTENTS,
  NOT_SELECTED_ERROR_SUMMARY_BEFORE,
  NOT_ADD_CRAS_DATA_TO,
  NOT_ADD_CRAS_DATA_SUBJECT,
  NOT_SELECTED_CRAS_DATA_BEFORE,
  NOT_ADD_MPA_VERSION_TO,
  NOT_ADD_MPA_VERSION_SUBJECT,
  NOT_SELECTED_MPA_VERSION_BEFORE,
  NOT_SELECTED_ANY_EMAIL,
}

export default function useRemoteJob(type: RemoteJobType) {
  const {
    initRemoteJob,
    selectSite,
    selectJobId,
    setRemoteJob,
    makeRequestData,
    setSelectSite,
    setSelectJobId,
    checkErrorData,
  } = useRemoteJobSlices();
  const [current, setCurrent] = useState(0);
  const history = useHistory();
  const { data, isFetching } = useQuery<ResGetRemoteJob>(
    ['get_remote_job', selectJobId],
    () => getRemoteJob(`${selectJobId}`),
    {
      enabled: !!selectJobId && type === 'edit',
      // retryOnMount: false,
      // refetchOnMount: false,
      refetchOnWindowFocus: false,
      initialData: undefined,
      onError: () => {
        openNotification('error', 'Error', `Failed to get remote job information "${selectSite?.label}".`);
      },
      onSuccess: (data) => {
        setRemoteJob(data);
      },
    }
  );
  const onBack = useCallback(() => {
    history.push(PAGE_URL.STATUS_REMOTE);
  }, [history]);

  const openConfirmModal = useCallback(() => {
    const typeFirstUpper = type === 'add' ? 'Add' : 'Edit';
    const confirm = Modal.confirm({
      className: `${type}_remote_job`,
      title: `${typeFirstUpper} Remote Job`,
      content: `Are you sure to ${type} remote job?`,
      onOk: async () => {
        diableCancelBtn();
        const reqData = makeRequestData();
        try {
          if (type === 'add') {
            await postRemoteJob(reqData);
          } else {
            await putRemoteJob({ jobId: selectJobId as string, data: reqData });
          }
          openNotification('success', 'Success', `Succeed to ${type} remote job '${selectSite?.label}'.`);
        } catch (e) {
          openNotification('error', 'Error', `Failed to ${type} job '${selectSite?.label}'!`);
        } finally {
          onBack();
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
  }, [type, selectJobId, onBack, makeRequestData]);

  const openWarningModal = useCallback((reason: REMOTE_ERROR) => {
    const warning = Modal.warning({
      title: 'Error',
      content: getRemoteErrorMsg(reason),
    });
  }, []);

  const nextAction = useCallback(() => {
    const reason = getRemoteErrorReason({
      current,
      ...checkErrorData,
    });

    if (reason === REMOTE_ERROR.NO_ERROR) {
      if (current === REMOTE_STEP.CONFIRM) openConfirmModal();
      return true;
    } else {
      openWarningModal(reason);
      return false;
    }
  }, [current, checkErrorData]);

  return {
    current,
    setCurrent,
    onBack,
    nextAction,
    initRemoteJob,
    setSelectSite,
    setSelectJobId,
  };
}

function getRemoteErrorMsg(reason: REMOTE_ERROR): string {
  switch (reason) {
    case REMOTE_ERROR.NOT_SELECTED_SITE:
      return 'Please select a site.';
    case REMOTE_ERROR.NOT_SELECTED_PLANS:
      return 'Please select plans.';
    case REMOTE_ERROR.NOT_SELECTED_SENDING_TIME:
      return 'Please add daily sending time.';
    case REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_TO:
      return 'Please add recipients of error summuary.';
    case REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_SUBJECT:
      return 'Please input subject of error summuary.';
    case REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_CONTENTS:
      return 'Please input body of error summuary.';
    case REMOTE_ERROR.NOT_SELECTED_ERROR_SUMMARY_BEFORE:
      return 'Please input before of error summuary greater than 0.';
    case REMOTE_ERROR.NOT_ADD_CRAS_DATA_TO:
      return 'Please add recipients of cras data.';
    case REMOTE_ERROR.NOT_ADD_CRAS_DATA_SUBJECT:
      return 'Please input subject of cras data.';
    case REMOTE_ERROR.NOT_SELECTED_CRAS_DATA_BEFORE:
      return 'Please input before of cras data greater than 0.';
    case REMOTE_ERROR.NOT_ADD_MPA_VERSION_TO:
      return 'Please add recipients of mpa version.';
    case REMOTE_ERROR.NOT_ADD_MPA_VERSION_SUBJECT:
      return 'Please input subject of mpa version.';
    case REMOTE_ERROR.NOT_SELECTED_MPA_VERSION_BEFORE:
      return 'Please input before of mpa version greater than 0.';
    case REMOTE_ERROR.NOT_SELECTED_ANY_EMAIL:
      return 'Please input one or more email settings.';
    default:
      return "What's error??";
  }
}

interface RemoteJobStateCurrent extends RemoteJobState {
  current: number;
}

function getRemoteErrorReason({
  current,
  selectSite,
  selectJobId,
  selectPlans,
  sendingTimes,
  errorSummary,
  crasData,
  mpaVersion,
}: RemoteJobStateCurrent): REMOTE_ERROR {
  switch (current) {
    case REMOTE_STEP.PLANS:
      if (selectSite === undefined) {
        return REMOTE_ERROR.NOT_SELECTED_SITE;
      }
      if (selectPlans.length <= 0) {
        return REMOTE_ERROR.NOT_SELECTED_PLANS;
      }
      break;
    case REMOTE_STEP.NOTICE:
      if (errorSummary.enable) {
        const { selectedTags, subject, body, before } = errorSummary;
        if (selectedTags.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_TO;
        }
        if (subject.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_SUBJECT;
        }
        if (body.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_ERROR_SUMMARY_CONTENTS;
        }
        if (before <= 0) {
          return REMOTE_ERROR.NOT_SELECTED_ERROR_SUMMARY_BEFORE;
        }
      }
      if (crasData.enable) {
        const { selectedTags, subject, before } = crasData;
        if (selectedTags.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_CRAS_DATA_TO;
        }
        if (subject.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_CRAS_DATA_SUBJECT;
        }
        if (before <= 0) {
          return REMOTE_ERROR.NOT_SELECTED_CRAS_DATA_BEFORE;
        }
      }
      if (mpaVersion.enable) {
        const { selectedTags, subject, before } = mpaVersion;
        if (selectedTags.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_MPA_VERSION_TO;
        }
        if (subject.length <= 0) {
          return REMOTE_ERROR.NOT_ADD_MPA_VERSION_SUBJECT;
        }
        if (before <= 0) {
          return REMOTE_ERROR.NOT_SELECTED_MPA_VERSION_BEFORE;
        }
      }
      if (sendingTimes.length <= 0 && (errorSummary.enable || crasData.enable || mpaVersion.enable)) {
        return REMOTE_ERROR.NOT_SELECTED_SENDING_TIME;
      }
      if (sendingTimes.length > 0 && !(errorSummary.enable || crasData.enable || mpaVersion.enable)) {
        return REMOTE_ERROR.NOT_SELECTED_ANY_EMAIL;
      }
      break;
  }

  return REMOTE_ERROR.NO_ERROR;
}
