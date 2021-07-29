import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { LabeledValue } from 'antd/lib/select';
import React from 'react';
import {
  CRAS_DATA_DEFAULT_BEFORE,
  ERROR_SUMMARY_DEFAULT_BEFORE,
  MPA_VERSION_DEFAULT_BEFORE,
} from '../../lib/constants';
import { RootState } from '../rootReducer';

export interface RemoteJobState {
  selectSite: LabeledValue | undefined;
  selectJobId: string | undefined;
  selectPlans: React.Key[];
  sendingTimes: string[];
  errorSummary: EmailOptionState;
  crasData: EmailOptionState;
  mpaVersion: EmailOptionState;
}

export interface EmailOptionState {
  enable: boolean;
  selectedTags: LabeledValue[];
  subject: string;
  body: string;
  before: number;
  showAddrBook: boolean;
}

export type EmailOptionStatePartial = Partial<EmailOptionState>;

export type EmailOptionStateKey = 'enable' | 'selectedTags' | 'subject' | 'body' | 'before' | 'showAddrBook';

const initialState: RemoteJobState = {
  selectSite: undefined,
  selectJobId: undefined,
  selectPlans: [],
  sendingTimes: [],
  errorSummary: {
    enable: false,
    selectedTags: [],
    subject: '',
    body: '',
    before: ERROR_SUMMARY_DEFAULT_BEFORE,
    showAddrBook: false,
  },
  crasData: {
    enable: false,
    selectedTags: [],
    subject: '',
    body: '',
    before: CRAS_DATA_DEFAULT_BEFORE,
    showAddrBook: false,
  },
  mpaVersion: {
    enable: false,
    selectedTags: [],
    subject: '',
    body: '',
    before: MPA_VERSION_DEFAULT_BEFORE,
    showAddrBook: false,
  },
};

const remoteJob = createSlice({
  name: 'remoteJob',
  initialState,
  reducers: {
    initRemoteJobReducer: () => initialState,
    selectSiteReducer(state, action: PayloadAction<LabeledValue>) {
      state.selectSite = action.payload;
    },
    selectJobIdReducer(state, action: PayloadAction<string | undefined>) {
      state.selectJobId = action.payload;
    },
    selectPlansReducer(state, action: PayloadAction<React.Key[]>) {
      state.selectPlans = action.payload;
    },
    sendingTimesReducer(state, action: PayloadAction<string[]>) {
      state.sendingTimes = action.payload;
    },
    errorSummaryReducer(state, action: PayloadAction<EmailOptionState>) {
      state.errorSummary = action.payload;
    },
    errorSummaryPartialReducer(state, action: PayloadAction<EmailOptionStatePartial>) {
      state.errorSummary = {
        ...state.errorSummary,
        ...action.payload,
      };
    },
    crasDataReducer(state, action: PayloadAction<EmailOptionState>) {
      state.crasData = action.payload;
    },
    crasDataPartialReducer(state, action: PayloadAction<EmailOptionStatePartial>) {
      state.crasData = {
        ...state.crasData,
        ...action.payload,
      };
    },
    mpaVersionReducer(state, action: PayloadAction<EmailOptionState>) {
      state.mpaVersion = action.payload;
    },
    mpaVersionPartialReducer(state, action: PayloadAction<EmailOptionStatePartial>) {
      state.mpaVersion = {
        ...state.crasData,
        ...action.payload,
      };
    },
  },
});

export const {
  initRemoteJobReducer,
  selectSiteReducer,
  selectJobIdReducer,
  selectPlansReducer,
  sendingTimesReducer,
  errorSummaryReducer,
  errorSummaryPartialReducer,
  crasDataReducer,
  crasDataPartialReducer,
  mpaVersionReducer,
  mpaVersionPartialReducer,
} = remoteJob.actions;

export const remoteJobSiteSelector = (state: RootState): RemoteJobState['selectSite'] | undefined =>
  state.remoteJob.selectSite;
export const remoteJobJobIdSelector = (state: RootState): RemoteJobState['selectJobId'] => state.remoteJob.selectJobId;
export const remoteJobPlansSelector = (state: RootState): RemoteJobState['selectPlans'] => state.remoteJob.selectPlans;
export const remoteJobSendingTimesSelector = (state: RootState): RemoteJobState['sendingTimes'] =>
  state.remoteJob.sendingTimes;
export const remoteJobErrorSummarySelector = (state: RootState): RemoteJobState['errorSummary'] =>
  state.remoteJob.errorSummary;
export const remoteJobCrasDataSelector = (state: RootState): RemoteJobState['crasData'] => state.remoteJob.crasData;
export const remoteJobMpaVersionSelector = (state: RootState): RemoteJobState['mpaVersion'] =>
  state.remoteJob.mpaVersion;

export default remoteJob.reducer;
