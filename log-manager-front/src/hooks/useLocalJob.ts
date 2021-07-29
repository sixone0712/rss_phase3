import { Modal } from 'antd';
import { useCallback, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { LOCAL_ERROR, LOCAL_STEP } from '../components/modules/LocalJob/LocalJob';
import { postLocalJob } from '../lib/api/axios/requests';
import { PAGE_URL } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import useLocalJobSlices from './useLocalJobSlices';
import useUploadFiles from './useUploadFiles';

interface FileIdType {
  fileId: string;
  uid: string;
  status: string;
}
export default function useLocalJob() {
  const [current, setCurrent] = useState(0);
  const { selectSite, initLocalJobState, setSelectSite } = useLocalJobSlices();
  const history = useHistory();

  // TODO: There is an error that cannot save the filelist of the upload component of antd to the store of redux.
  // Alternatively, use use-global-hook to share values between hooks.
  // In the future, you will need to move to redux when this problem is resolved.
  const { uploadFiles, setUploadFiles, responseFiles, setResponseFiles, initUploadFiles } = useUploadFiles();

  const makeRequestData = useCallback(
    () => ({
      siteId: selectSite?.value === undefined ? 0 : (selectSite.value as number),

      fileIndices: responseFiles.reduce((acc, cur) => {
        if (cur.status === 'done' && cur.fileIndex !== undefined) return acc.concat(cur.fileIndex);
        else return acc;
      }, <number[]>[]),
    }),
    [selectSite, responseFiles]
  );

  const onBack = useCallback(() => {
    history.push(PAGE_URL.STATUS_LOCAL);
  }, [history]);

  const openAddModal = useCallback(() => {
    const confirm = Modal.confirm({
      className: 'add-local-job',
      title: 'Add Local Job',
      content: 'Are you sure to add local job?',
      onOk: async () => {
        diableCancelBtn();
        const reqData = makeRequestData();
        try {
          await postLocalJob(reqData);
          openNotification('success', 'Success', 'Succeed to add local job.');
        } catch (e) {
          openNotification('error', 'Error', 'Failed to add local job!');
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
  }, [makeRequestData, onBack]);

  const openWarningModal = useCallback((reason: number) => {
    let content = '';
    switch (reason) {
      case LOCAL_ERROR.NOT_SELECTED_SITE:
        content = 'Please select a site.';
        break;
      case LOCAL_ERROR.NOT_UPLOADED_FILES:
        content = 'Please load a file.';
        break;
      case LOCAL_ERROR.UPLOADING_FILES:
        content = 'File is being uploaded. Please wait until the upload is finished.';
    }
    const warning = Modal.warning({
      title: 'Error',
      content,
    });
  }, []);

  const nextAction = useCallback(() => {
    switch (current) {
      case LOCAL_STEP.CONFIGURE:
        if (selectSite === undefined) {
          openWarningModal(LOCAL_ERROR.NOT_SELECTED_SITE);
          return false;
        }

        if (responseFiles.filter((item) => item.status === 'uploading').length > 0) {
          openWarningModal(LOCAL_ERROR.UPLOADING_FILES);
          return false;
        }
        if (responseFiles.filter((item) => item.status === 'done').length === 0) {
          openWarningModal(LOCAL_ERROR.NOT_UPLOADED_FILES);
          return false;
        }
        break;
      case LOCAL_STEP.CONFIRM:
        openAddModal();
        break;
    }
    return true;
  }, [current, selectSite, responseFiles]);

  const initLocalJob = useCallback(() => {
    initLocalJobState();
    initUploadFiles();
  }, [initLocalJobState, initUploadFiles]);

  return {
    current,
    setCurrent,
    nextAction,
    onBack,
    initLocalJob,
  };
}
