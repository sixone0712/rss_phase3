import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { BuildStatus, StatusStepType, StatusType } from '../../types/status';
import { RootState } from '../rootReducer';

export interface BuildHistorySelectedLogState {
  id: number | undefined;
  status: BuildStatus | undefined;
  name: string;
}

export interface BuildHistorySelectedJobState {
  jobId: string | undefined;
  type: StatusType | undefined;
  stepType: StatusStepType | undefined;
}

export interface BuildHistoryState {
  selectedJob: BuildHistorySelectedJobState | undefined;
  selectedLog: BuildHistorySelectedLogState | undefined;
}

const initialState: BuildHistoryState = {
  selectedJob: undefined,
  selectedLog: undefined,
};

const buildHistory = createSlice({
  name: 'buildHistory',
  initialState,
  reducers: {
    initBuildHistory: () => initialState,
    setBuildHistorySelectedLog(state, action: PayloadAction<BuildHistorySelectedLogState | undefined>) {
      state.selectedLog = action.payload;
    },
    setBuildHistorySelectedJob(state, action: PayloadAction<BuildHistorySelectedJobState | undefined>) {
      state.selectedJob = action.payload;
    },
  },
});

export const { initBuildHistory, setBuildHistorySelectedLog, setBuildHistorySelectedJob } = buildHistory.actions;

export const buildHistorySelectedJob = (state: RootState): BuildHistoryState['selectedJob'] =>
  state.buildHistory.selectedJob;
export const buildHistorySelectedLog = (state: RootState): BuildHistoryState['selectedLog'] =>
  state.buildHistory.selectedLog;

export default buildHistory.reducer;
