import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { LabeledValue } from 'antd/lib/select';
import { RootState } from '../rootReducer';

interface LocalJobState {
  selectSite: LabeledValue | undefined;
  files: any;
}

const initialState: LocalJobState = {
  selectSite: undefined,
  files: [],
};

const locaJob = createSlice({
  name: 'localJob',
  initialState,
  reducers: {
    initLocalJob: () => initialState,
    selectSite(state, action: PayloadAction<LabeledValue>) {
      state.selectSite = action.payload;
    },
    uploadFiles(state, action: PayloadAction<any>) {
      state.files = action.payload;
    },
  },
});

export const {
  initLocalJob: initLocalJobAction,
  selectSite: selectSiteAction,
  uploadFiles: uploadFilesAction,
} = locaJob.actions;

export const localJobSiteSelector = (state: RootState): LocalJobState['selectSite'] => state.localJob.selectSite;
export const localJobFilesSelector = (state: RootState): LocalJobState['files'] => state.localJob.files;

export default locaJob.reducer;
