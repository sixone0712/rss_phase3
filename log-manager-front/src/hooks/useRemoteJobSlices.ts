import { LabeledValue } from 'antd/lib/select';
import React, { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { v4 as uuidv4 } from 'uuid';
import { AddressInfo, ReqPostRemoteJob, ResGetRemoteJob } from '../lib/api/axios/types';
import { CRAS_DATA_DEFAULT_BEFORE, ERROR_SUMMARY_DEFAULT_BEFORE, MPA_VERSION_DEFAULT_BEFORE } from '../lib/constants';
import {
  crasDataPartialReducer,
  crasDataReducer,
  EmailOptionState,
  EmailOptionStatePartial,
  errorSummaryPartialReducer,
  errorSummaryReducer,
  initRemoteJobReducer,
  mpaVersionPartialReducer,
  mpaVersionReducer,
  remoteJobCrasDataSelector,
  remoteJobErrorSummarySelector,
  remoteJobJobIdSelector,
  remoteJobMpaVersionSelector,
  remoteJobPlansSelector,
  remoteJobSendingTimesSelector,
  remoteJobSiteSelector,
  selectJobIdReducer,
  selectPlansReducer,
  selectSiteReducer,
  sendingTimesReducer,
} from '../reducers/slices/remoteJob';

export default function useRemoteJobSlices() {
  const selectSite = useSelector(remoteJobSiteSelector);
  const selectJobId = useSelector(remoteJobJobIdSelector);
  const selectPlans = useSelector(remoteJobPlansSelector);
  const sendingTimes = useSelector(remoteJobSendingTimesSelector);
  const errorSummary = useSelector(remoteJobErrorSummarySelector);
  const crasData = useSelector(remoteJobCrasDataSelector);
  const mpaVersion = useSelector(remoteJobMpaVersionSelector);
  const dispatch = useDispatch();

  const initRemoteJob = useCallback(() => {
    dispatch(initRemoteJobReducer());
  }, [dispatch]);

  const setSelectSite = useCallback(
    ({ value, label }: LabeledValue) => {
      dispatch(selectSiteReducer({ value, label }));
    },
    [dispatch]
  );

  const setSelectJobId = useCallback(
    (value: string | undefined) => {
      dispatch(selectJobIdReducer(value));
    },
    [dispatch]
  );

  const setSelectPlans = useCallback(
    (value: React.Key[]) => {
      dispatch(selectPlansReducer(value));
    },
    [dispatch]
  );

  const setSendingTimes = useCallback(
    (value: string[]) => {
      dispatch(sendingTimesReducer(value));
    },
    [dispatch]
  );

  const setAllErrorSummary = useCallback(
    (value: EmailOptionState) => {
      dispatch(errorSummaryReducer(value));
    },
    [dispatch]
  );

  const setPartialErrorSummary = useCallback(
    (value: EmailOptionStatePartial) => {
      dispatch(errorSummaryPartialReducer(value));
    },
    [dispatch]
  );

  const setAllCrasData = useCallback(
    (value: EmailOptionState) => {
      dispatch(crasDataReducer(value));
    },
    [dispatch]
  );

  const setPartialCrasData = useCallback(
    (value: EmailOptionStatePartial) => {
      dispatch(crasDataPartialReducer(value));
    },
    [dispatch, crasData]
  );

  const setAllMpaVersion = useCallback(
    (value: EmailOptionState) => {
      dispatch(mpaVersionReducer(value));
    },
    [dispatch]
  );

  const setPartialMpaVersion = useCallback(
    (value: EmailOptionStatePartial) => {
      dispatch(mpaVersionPartialReducer(value));
    },
    [dispatch, mpaVersion]
  );

  const convertToSelectedTags = useCallback(
    (groups: AddressInfo[] | undefined, emails: AddressInfo[] | undefined, recipients: string[] | undefined) => {
      const selectedTags: LabeledValue[] = [];
      if (groups && groups.length > 0) {
        groups.map((item) =>
          selectedTags.push({ key: `group_${item.id}`, value: `group_${item.id}`, label: `@${item.name}` })
        );
      }

      if (emails && emails.length > 0) {
        emails.map((item) =>
          selectedTags.push({
            key: `email_${item.id}`,
            value: `email_${item.id}`,
            label: `${item.name} <${item.email}>`,
          })
        );
      }

      if (recipients && recipients.length > 0) {
        recipients.map((item) => selectedTags.push({ key: uuidv4(), value: uuidv4(), label: item }));
      }
      return selectedTags;
    },
    []
  );

  const setRemoteJob = useCallback(
    (data: ResGetRemoteJob) => {
      const {
        planIds,
        sendingTimes,
        isErrorSummary,
        isCrasData,
        isMpaVersion,
        errorSummary,
        crasData,
        mpaVersion,
      } = data;

      setSelectPlans(planIds);
      setSendingTimes(sendingTimes);
      setAllErrorSummary({
        enable: isErrorSummary,
        subject: errorSummary ? errorSummary.subject : '',
        selectedTags: convertToSelectedTags(
          errorSummary?.groupBook,
          errorSummary?.emailBook,
          errorSummary?.customEmails
        ),
        body: errorSummary ? errorSummary.body : '',
        before: errorSummary ? errorSummary.before : ERROR_SUMMARY_DEFAULT_BEFORE,
        showAddrBook: false,
      });
      setAllCrasData({
        enable: isCrasData,
        selectedTags: convertToSelectedTags(crasData?.groupBook, crasData?.emailBook, crasData?.customEmails),
        subject: crasData ? crasData.subject : '',
        body: '',
        before: crasData ? crasData.before : CRAS_DATA_DEFAULT_BEFORE,
        showAddrBook: false,
      });
      setAllMpaVersion({
        enable: isMpaVersion,
        selectedTags: convertToSelectedTags(mpaVersion?.groupBook, mpaVersion?.emailBook, mpaVersion?.customEmails),
        subject: mpaVersion ? mpaVersion.subject : '',
        body: '',
        before: mpaVersion ? mpaVersion.before : MPA_VERSION_DEFAULT_BEFORE,
        showAddrBook: false,
      });
    },
    [
      setSelectPlans,
      setSendingTimes,
      setPartialErrorSummary,
      setPartialCrasData,
      setPartialMpaVersion,
      convertToSelectedTags,
    ]
  );

  const convertToCustomEmails = useCallback((tags: LabeledValue[]): string[] => {
    return tags
      .filter((item) => !item.value.toString().startsWith('group_') && !item.value.toString().startsWith('email_'))
      .map((filtered) => filtered.label as string);
  }, []);

  const convertToGroupBookIds = useCallback((tags: LabeledValue[]): number[] => {
    return tags
      .filter((item) => item.value.toString().startsWith('group_'))
      .map((filtered) => +filtered.value.toString().replace('group_', ''));
  }, []);

  const convertToEmailBookIds = useCallback((tags: LabeledValue[]): number[] => {
    return tags
      .filter((item) => item.value.toString().startsWith('email_'))
      .map((filtered) => +filtered.value.toString().replace('email_', ''));
  }, []);

  const makeRequestData = useCallback((): ReqPostRemoteJob => {
    return {
      siteId: selectSite?.value === undefined ? 0 : +`${selectSite.value}`,
      planIds: selectPlans as number[],
      jobType: 'remote',
      sendingTimes: sendingTimes,
      isErrorSummary: errorSummary.enable,
      errorSummary: !errorSummary.enable
        ? undefined
        : {
            customEmails: errorSummary ? convertToCustomEmails(errorSummary.selectedTags) : [],
            groupBookIds: errorSummary ? convertToGroupBookIds(errorSummary.selectedTags) : [],
            emailBookIds: errorSummary ? convertToEmailBookIds(errorSummary.selectedTags) : [],
            subject: errorSummary.subject,
            body: errorSummary.body,
            before: errorSummary.before,
          },
      isCrasData: crasData.enable,
      crasData: !crasData.enable
        ? undefined
        : {
            customEmails: crasData ? convertToCustomEmails(crasData.selectedTags) : [],
            groupBookIds: crasData ? convertToGroupBookIds(crasData.selectedTags) : [],
            emailBookIds: crasData ? convertToEmailBookIds(crasData.selectedTags) : [],
            subject: crasData.subject,
            body: crasData.body,
            before: crasData.before,
          },
      isMpaVersion: mpaVersion.enable,
      mpaVersion: !mpaVersion.enable
        ? undefined
        : {
            customEmails: mpaVersion ? convertToCustomEmails(mpaVersion.selectedTags) : [],
            groupBookIds: mpaVersion ? convertToGroupBookIds(mpaVersion.selectedTags) : [],
            emailBookIds: mpaVersion ? convertToEmailBookIds(mpaVersion.selectedTags) : [],
            subject: mpaVersion.subject,
            body: mpaVersion.body,
            before: mpaVersion.before,
          },
    };
  }, [selectSite, selectPlans, sendingTimes, errorSummary, crasData, mpaVersion]);

  return {
    initRemoteJob,
    selectSite,
    setSelectSite,
    selectJobId,
    setSelectJobId,
    selectPlans,
    setSelectPlans,
    sendingTimes,
    setSendingTimes,
    errorSummary,
    setPartialErrorSummary,
    crasData,
    setPartialCrasData,
    mpaVersion,
    setPartialMpaVersion,
    setRemoteJob,
    makeRequestData,
    checkErrorData: { selectSite, selectJobId, selectPlans, sendingTimes, errorSummary, crasData, mpaVersion },
  };
}
