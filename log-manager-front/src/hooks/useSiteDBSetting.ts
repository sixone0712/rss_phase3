import { useForm } from 'antd/lib/form/Form';
import { SwitchClickEventHandler } from 'antd/lib/switch';
import { PresetStatusColorType } from 'antd/lib/_util/colors';
import { AxiosError } from 'axios';
import { useCallback, useMemo, useState } from 'react';
import { useIsMutating, useMutation, useQueryClient } from 'react-query';
import { useDispatch, useSelector } from 'react-redux';
import {
  postCrasConnection,
  postEmailConnection,
  postRssConnection,
  postSiteDBInfo,
  putSiteDBInfo,
} from '../lib/api/axios/requests';
import {
  ClientError,
  ReqPostCrasConnection,
  ReqPostEmailConnection,
  ReqPostRssConnection,
  ReqPostSiteDBInfo,
  ReqPutSiteDBInfo,
  SiteDBInfo,
} from '../lib/api/axios/types';
import md5 from '../lib/api/md5';
import { CRAS_LOCALHOST_NAME, DEFAULT_PASSWORD_VALUE } from '../lib/constants';
import { openNotification } from '../lib/util/notification';
import {
  setSiteInfoDrawer,
  SiteDrawerOpenType,
  siteInfoDrawerType,
  siteInfoIsDrawer,
  siteInfoSelectedSite,
} from '../reducers/slices/configure';

export type TestConnStatusType = PresetStatusColorType | 'error(cras_info)';

export default function useSiteDBSetting() {
  const queryClient = useQueryClient();
  const dispatch = useDispatch();
  const [crasStatus, setCrasStatus] = useState<TestConnStatusType>('default');
  const [emailStatus, setEmailStatus] = useState<TestConnStatusType>('default');
  const [rssStatus, setRssStatus] = useState<TestConnStatusType>('default');
  const [localRssPassword, setLocalRssPassword] = useState('');
  const [localEmailPassword, setLocalEmailPassword] = useState('');
  const isDrawer = useSelector(siteInfoIsDrawer);
  const drawerType = useSelector(siteInfoDrawerType);
  const selectedSite = useSelector(siteInfoSelectedSite);
  const [form] = useForm<SiteDBInfo>();
  const [localhost, setLocalhost] = useState(false);

  const crasMutation = useMutation((postData: ReqPostCrasConnection) => postCrasConnection(postData), {
    mutationKey: 'connection_test_cras_server',
    onError: () => {
      setCrasStatus('error');
    },
    onSuccess: () => {
      setCrasStatus('success');
    },
  });
  const emailMutation = useMutation((postData: ReqPostEmailConnection) => postEmailConnection(postData), {
    mutationKey: 'connection_test_email_server',
    onError: () => {
      setEmailStatus('error');
    },
    onSuccess: () => {
      setEmailStatus('success');
    },
  });
  const rssMutation = useMutation((postData: ReqPostRssConnection) => postRssConnection(postData), {
    mutationKey: 'connection_test_rss_server',
    onError: () => {
      setRssStatus('error');
    },
    onSuccess: () => {
      setRssStatus('success');
    },
  });
  const addMutation = useMutation((postData: ReqPostSiteDBInfo) => postSiteDBInfo(postData), {
    mutationKey: 'add_config_site_db_info',
    onError: (resData: AxiosError<ClientError>) => {
      openNotification('error', 'Error', resData.response?.data.errorMsg ?? 'Failed to add settings server info.');
      if (resData.response?.status !== 400) {
        // not bad request
        queryClient.fetchQuery('get_config_site_db_info');
        dispatch(setSiteInfoDrawer(false));
      }
    },
    onSuccess: () => {
      queryClient.fetchQuery('get_config_site_db_info');
      dispatch(setSiteInfoDrawer(false));
      openNotification('success', 'Success', 'Succeed to add settings server info.');
    },
  });
  const editMutation = useMutation((reqData: ReqPutSiteDBInfo) => putSiteDBInfo(reqData), {
    mutationKey: 'modify_config_site_db_info',
    onError: (resData: AxiosError<ClientError>) => {
      openNotification('error', 'Error', resData.response?.data.errorMsg ?? 'Failed to edit settings server info.');
      if (resData.response?.status !== 400) {
        // not bad request
        queryClient.fetchQuery('get_config_site_db_info');
        dispatch(setSiteInfoDrawer(false));
      }
    },
    onSuccess: () => {
      queryClient.fetchQuery('get_config_site_db_info');
      dispatch(setSiteInfoDrawer(false));
      openNotification('success', 'Success', 'Succeed to edit settings server info.');
    },
  });

  const isMutatingEdit = useIsMutating({ mutationKey: ['modify_config_site_db_info'] }) > 0 ? true : false;
  const isMutatingAdd = useIsMutating({ mutationKey: ['add_config_site_db_info'] }) > 0 ? true : false;
  const isRequestAddEdit = isMutatingEdit || isMutatingAdd;

  const testCrasServer = useCallback(
    (postData: ReqPostCrasConnection) => {
      setCrasStatus('processing');
      crasMutation.mutate(postData);
    },
    [crasMutation, setCrasStatus]
  );

  const testEmailServer = useCallback(
    (postData: ReqPostEmailConnection) => {
      setEmailStatus('processing');
      if (postData.emailPassword === DEFAULT_PASSWORD_VALUE) postData.emailPassword = localEmailPassword;
      emailMutation.mutate(postData);
    },
    [emailMutation, setEmailStatus, localEmailPassword]
  );

  const testRssServer = useCallback(
    (postData: ReqPostRssConnection) => {
      setRssStatus('processing');
      if (postData.rssPassword === DEFAULT_PASSWORD_VALUE) {
        postData.rssPassword = localRssPassword;
      } else {
        if (postData.rssPassword) postData.rssPassword = md5(postData.rssPassword);
        else postData.rssPassword = '';
      }
      rssMutation.mutate(postData);
    },
    [rssMutation, setRssStatus, localRssPassword]
  );

  const initStatus = useCallback(() => {
    setCrasStatus('default');
    setEmailStatus('default');
    setRssStatus('default');
  }, [setCrasStatus, setEmailStatus, setRssStatus]);

  const initFormData = useCallback(() => {
    if (isDrawer && drawerType === 'edit') {
      if (selectedSite !== undefined) {
        const formData = Object.entries(selectedSite).map(([key, value]) => {
          if (key === 'emailPassword') {
            setLocalEmailPassword(value);
            value = DEFAULT_PASSWORD_VALUE;
          }
          if (key === 'rssPassword') {
            setLocalRssPassword(value);
            value = DEFAULT_PASSWORD_VALUE;
          }

          if (key === 'crasAddress' && value === CRAS_LOCALHOST_NAME) {
            setLocalhost(true);
            value = 'localhost';
          }

          return {
            name: key,
            value: value,
          };
        });
        form.setFields(formData);
      }
    } else {
      form.resetFields();
    }
  }, [isDrawer, drawerType, selectedSite, form]);

  const closeDrawer = useCallback(() => {
    if (isMutatingEdit || isMutatingAdd) return;
    dispatch(setSiteInfoDrawer(false));
  }, [dispatch, isMutatingEdit, isMutatingAdd]);

  const requestAdd = useCallback(
    (reqData: ReqPostSiteDBInfo) => {
      // MD5 encryption
      reqData.rssPassword = md5(reqData.rssPassword);
      addMutation.mutate(reqData);
    },
    [addMutation]
  );

  const requestEdit = useCallback(
    (reqData: ReqPutSiteDBInfo) => {
      if (reqData.emailAddress === DEFAULT_PASSWORD_VALUE) {
        reqData.emailAddress = localEmailPassword;
      }

      if (reqData.rssPassword === DEFAULT_PASSWORD_VALUE) {
        reqData.rssPassword = localRssPassword;
      } else {
        // MD5 encryption
        reqData.rssPassword = md5(reqData.rssPassword);
      }

      editMutation.mutate(reqData);
    },
    [editMutation, localRssPassword]
  );

  const serverRequest = useCallback(
    (type: SiteDrawerOpenType, data: SiteDBInfo) => {
      if (data.crasAddress === 'localhost') data.crasAddress = CRAS_LOCALHOST_NAME;
      if (type === 'add') requestAdd(data);
      else requestEdit(data);
    },
    [requestAdd, requestEdit]
  );

  const requestCrasStatus = useCallback(() => {
    const crasAddress = form.getFieldValue('crasAddress');
    const crasPort = form.getFieldValue('crasPort');
    testCrasServer({ crasAddress, crasPort });
  }, [form, testCrasServer]);

  const requestEmailStatus = useCallback(() => {
    const emailAddress = form.getFieldValue('emailAddress');
    const emailPort = form.getFieldValue('emailPort');
    const emailUserName = form.getFieldValue('emailUserName');
    const emailPassword = form.getFieldValue('emailPassword');
    testEmailServer({ emailAddress, emailPort, emailUserName, emailPassword });
  }, [form, testEmailServer]);

  const requestRssStatus = useCallback(() => {
    const rssAddress = form.getFieldValue('rssAddress');
    const rssPort = form.getFieldValue('rssPort');
    const rssUserName = form.getFieldValue('rssUserName');
    const rssPassword = form.getFieldValue('rssPassword');
    const crasAddress = form.getFieldValue('crasAddress');
    const crasPort = form.getFieldValue('crasPort');

    if (!crasAddress || !crasPort) {
      // let item;
      // if (!crasAddress && !crasPort) item = `'Address' and 'Port'`;
      // else if (!crasAddress) item = `'Address'`;
      // else if (!crasPort) item = `'Port'`;

      // Modal.warning({
      //   title: 'Warning',
      //   content: `Please input ${item} of Cras Server Setting!`,
      // });
      setRssStatus('error(cras_info)');
    } else {
      testRssServer({ rssAddress, rssPort, rssUserName, rssPassword, crasAddress, crasPort });
    }
  }, [form, testRssServer, setRssStatus]);

  const disabledCrasInput = useMemo(() => crasStatus === 'processing' || rssStatus === 'processing', [
    crasStatus,
    rssStatus,
  ]);

  const disabledCrasIPAddressInput = useMemo(
    () => crasStatus === 'processing' || rssStatus === 'processing' || localhost,
    [localhost, crasStatus, rssStatus]
  );

  const disabledRssInput = useMemo(() => rssStatus === 'processing', [rssStatus]);

  const disabledEmailInput = useMemo(() => emailStatus === 'processing', [emailStatus]);

  const disabledApply = useMemo(
    () => crasStatus === 'processing' || emailStatus === 'processing' || rssStatus === 'processing' || isRequestAddEdit,
    [crasStatus, emailStatus, rssStatus, isRequestAddEdit]
  );

  const onFinish = useCallback(
    (value: SiteDBInfo) => {
      serverRequest(drawerType, value);
    },
    [drawerType, serverRequest]
  );

  const resetRssPassword = () => {
    form.setFieldsValue({
      rssPassword: '',
    });
  };

  const resetEmailPassword = () => {
    form.setFieldsValue({
      emailPassword: '',
    });
  };

  const onClickLocalhost: SwitchClickEventHandler = useCallback(
    (check, event) => {
      if (check) {
        setLocalhost(true);
        form.setFieldsValue({
          crasAddress: 'localhost',
        });
      } else {
        form.resetFields(['crasAddress']);
        setLocalhost(false);
      }
    },
    [setLocalhost, form]
  );

  return {
    form,
    onFinish,
    crasStatus,
    emailStatus,
    rssStatus,
    initStatus,
    isDrawer,
    drawerType,
    closeDrawer,
    isRequestAddEdit,
    localhost,
    onClickLocalhost,
    requestCrasStatus,
    requestEmailStatus,
    requestRssStatus,
    disabledCrasInput,
    disabledCrasIPAddressInput,
    disabledEmailInput,
    disabledRssInput,
    resetRssPassword,
    resetEmailPassword,
    disabledApply,
    initFormData,
  };
}
